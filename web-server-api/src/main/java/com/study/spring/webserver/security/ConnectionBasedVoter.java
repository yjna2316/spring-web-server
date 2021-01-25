package com.study.spring.webserver.security;


import com.study.spring.webserver.model.commons.Id;
import com.study.spring.webserver.model.user.User;
import com.study.spring.webserver.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.commons.lang3.ClassUtils.isAssignable;

public class ConnectionBasedVoter implements AccessDecisionVoter<FilterInvocation> {

  private final RequestMatcher requiresAuthorizationRequestMatcher;

  private final Function<String, Id<User, Long>> idExtractor;

  private UserService userService;

  public ConnectionBasedVoter(RequestMatcher requiresAuthorizationRequestMatcher, Function<String, Id<User, Long>> idExtractor) {
    checkNotNull(requiresAuthorizationRequestMatcher, "requiresAuthorizationRequestMatcher must be provided.");
    checkNotNull(idExtractor, "idExtractor must be provided.");

    this.requiresAuthorizationRequestMatcher = requiresAuthorizationRequestMatcher;
    this.idExtractor = idExtractor; // URL(String)에서 UserId를 추출해주는 람다함수. apply 메소드를 이용해 람다함수를 실행할 수 있다.
  }

  @Override
  /**
   * Authentication: 인증 주체 (로그인 전/후 포괄)
   *    > principal 로그인전: 로그인아이디 등 로그인에 필요한 데이터
   *                로그인후: 로그인된 사용자를 잘 표현할 수 있는 모델(PK, 이름 등 포함) 데이터
   * FilterInvocation: 보호 받는 자원, url로 표현되는 리소스
   * Collection<ConfigAttribute>: 보호 받는 자원과 관련된 리소스
   */
  public int vote(Authentication authentication, FilterInvocation fi, Collection<ConfigAttribute> attributes) {
    /* 접근 대상 리소스가 본인 또는 친구관계인지 확인하고 접근 혀용/거절 처리 구현 */
    HttpServletRequest request = fi.getRequest();

    /*
     * 특정 패턴 API와 매칭되지 않는 요청일 경우 => 승인 처리
     * 요청 URI가 특정 패턴 API(/api/user/{userId}/)에 매칭되지 않아, 지원하는 url이 아닐 때
     * 로직 구성상 보류 처리를 해주어도 크게 문제는 없지만,
     * WebExpressionVoter가 앞에서 승인했기 때문에 결과적으로 승인처리 되므로 승인으로 판단하였다.
     */
    if (!requiresAuthorizationRequestMatcher.matches(request)) {
      return ACCESS_GRANTED;
    }

    /*
     * Authentication 인스턴스가 JwtAuthenticationToken 타입으로 캐스팅 가능한지 확인
     * 다른 종류의 토큰 타입일 경우 authentication 객체를 해석할 수 있는 능력이 없기 때문에 => 보류 처리
     * 내가 처리할 수 있는 정보가 부족하다로 판단함
     */
    if (!isAssignable(JwtAuthenticationToken.class, authentication.getClass())) {
      return ACCESS_ABSTAIN;
    }

    JwtAuthentication jwtAuthentication = (JwtAuthentication) authentication.getPrincipal();
    Id<User, Long> userId = jwtAuthentication.id;
    /*
     * Applies this function to the given argument.
     * apply 메소드를 통해 요청 URL을 람다함수에 적용해서 유저 ID를 추출해온다.
     */
    Id<User, Long> targetId = idExtractor.apply(request.getRequestURI());


    if (userId.equals(targetId)) { // 본인 리소스에 접근이면 PASS
      return ACCESS_GRANTED;
    } else {                       // 친구의 리소스에 접근도 PASS
      List<Id<User, Long>> connetedIds = userService.findConnectedIds(userId);
      if (connetedIds.contains(targetId)) {
        return ACCESS_GRANTED;
      }
    }

    return ACCESS_DENIED;
  }

  /**
   *  targetUrl을 ConfigAttribute에서 꺼내 올 수 있다면 supports 메소드를 활용해도 된다.
   *  하지만, ConfigAttribute에 어떤 값을 담고 있는지 알 수 없어서 사용하지 않았다.
   */
  @Override
  public boolean supports(ConfigAttribute attribute) {
    return true;
  }

  @Override
  public boolean supports(Class<?> clazz) {
    return isAssignable(FilterInvocation.class, clazz);
  }

  @Autowired
  public void setUserService(UserService userService) {
    this.userService = userService;
  }
}