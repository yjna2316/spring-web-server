package com.study.spring.webserver.security;


import com.study.spring.webserver.model.commons.Id;
import com.study.spring.webserver.model.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.commons.lang3.ClassUtils.isAssignable;

/**
 * 전략 패턴을 이용한 ConnectionBasedVoter 구현
 * 전략 패턴을 이용하면 요구사항이 추가되더라도(ex.친구의 친구 포스트까지 허용) 기존 코드는 변경 없이 반영할 수 있다.
 * OCP 원칙 적용: 변경에 닫혀있고 확장에 열려있다
 */
public class ConnectionBasedVoterStrategy implements AccessDecisionVoter<FilterInvocation> {

  private final RequestMatcher requiresAuthorizationRequestMatcher;

  private final Function<String, Id<User, Long>> idExtractor;

  private GrantStrategies grantStrategies;

  public ConnectionBasedVoterStrategy(RequestMatcher requiresAuthorizationRequestMatcher, Function<String, Id<User, Long>> idExtractor) {
    checkNotNull(requiresAuthorizationRequestMatcher, "requiresAuthorizationRequestMatcher must be provided.");
    checkNotNull(idExtractor, "idExtractor must be provided.");

    this.requiresAuthorizationRequestMatcher = requiresAuthorizationRequestMatcher;
    // URL(String)에서 UserId를 추출해주는 람다함수. apply 메소드를 이용해 람다함수를 실행했다.
    this.idExtractor = idExtractor;
  }

  /* 접근 대상 리소스가 본인 또는 친구관계인지 확인하고 접근 혀용/거절 처리 구현 */
  @Override
  public int vote(Authentication authentication, FilterInvocation fi, Collection<ConfigAttribute> attributes) {
    HttpServletRequest request = fi.getRequest();

    // 특정 패턴 API와 매칭되지 않는 요청일 경우 => 승인 처리 */
    if (!requiresAuthorizationRequestMatcher.matches(request)) {
      return ACCESS_GRANTED;
    }

    // Authentication 인스턴스가 JwtAuthenticationToken 타입으로 캐스팅 가능한지 확인
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

    return grantStrategies.grant(userId, targetId) ? ACCESS_GRANTED : ACCESS_DENIED;
  }

  @Override
  public boolean supports(ConfigAttribute attribute) {
    return true;
  }

  @Override
  public boolean supports(Class<?> clazz) {
    return isAssignable(FilterInvocation.class, clazz);
  }

  @Autowired
  public void setUserGrantStrategies(GrantStrategies grantStrategies) {
    this.grantStrategies = grantStrategies;
  }
}