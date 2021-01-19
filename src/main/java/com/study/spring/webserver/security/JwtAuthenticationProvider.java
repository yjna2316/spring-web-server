package com.study.spring.webserver.security;

import com.study.spring.webserver.error.NotFoundException;
import com.study.spring.webserver.model.user.Email;
import com.study.spring.webserver.model.user.Role;
import com.study.spring.webserver.model.user.User;
import com.study.spring.webserver.service.user.UserService;
import org.springframework.dao.DataAccessException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.apache.commons.lang3.ClassUtils.isAssignable;
import static org.springframework.security.core.authority.AuthorityUtils.createAuthorityList;

/**
 * JwtAuthenticationProvider
 * - JwtAuthenticationToken을 처리할 수 있는 provider
 * - 실질적인 사용자 인증 처리 로직을 수행하고 JWT를 생성하는 곳. UserService를 통해 사용자 정보를 DB에서 조회한다.
 * - JwtAuthenticationToken은 커스텀 객체이기 때문에 이를 처리할 수 있는 provider가 존재하지 않는다. 그러므로 provider를 직접 만들어주었다.
 * - 인증 결과는 JwtAuthenticationToken 타입으로 반환한다.
 * - AuthenticationManager는 provider 목록 리스트를 들고 있으며, suppors 메소드 구현을 통해 어떤 타입의 Token을 처리할 수 있는지 AuthenticationManager에게 알려준다.
 * - 스프링 시큐리티 기본 구현체 (DaoAuthenticationProvider)와 비슷한 역할 수행
 */
public class JwtAuthenticationProvider implements AuthenticationProvider {

  private final Jwt jwt;

  private final UserService userService;

  public JwtAuthenticationProvider(Jwt jwt, UserService userService) {
    this.jwt = jwt;
    this.userService = userService;
  }

  @Override
  /**
   * authenticate() - 실제 사용자 인증을 수행하는 메소드
   **/
  public Authentication authenticate(Authentication authentication) throws AuthenticationException {
    JwtAuthenticationToken authenticationToken = (JwtAuthenticationToken) authentication;
    return processUserAuthentication(authenticationToken.authenticationRequest());
  }

  private Authentication processUserAuthentication(AuthenticationRequest request) {
    try {
      User user = userService.login(new Email(request.getPrincipal()), request.getCredentials());

      // 응답을 위한 인증 정보 객체(Authentication Token)을 만들어서 내려준다. 권한정보도 같이 넣어준다.
      JwtAuthenticationToken authenticated =
        new JwtAuthenticationToken(new JwtAuthentication(user.getSeq(), user.getEmail(), user.getName()), null, createAuthorityList(Role.USER.value()));

      String apiToken = user.newApiToken(jwt, new String[]{Role.USER.value()});

      // detail에 토큰과 유저정보를 넣어준다.
      authenticated.setDetails(new AuthenticationResult(apiToken, user));

      return authenticated;
    } catch (NotFoundException e) {
      throw new UsernameNotFoundException(e.getMessage());
    } catch (IllegalArgumentException e) {
      throw new BadCredentialsException(e.getMessage());
    } catch (DataAccessException e) {
      throw new AuthenticationServiceException(e.getMessage(), e);
    }
  }

  /**
   * supports() - provider가 처리할 수 있는 대상을 알려주는 메소드
   **/
  @Override
  public boolean supports(Class<?> authentication) {
    // 나는 요 토큰은 처리 할 수 있어 선언 -> JwtAuthenticationToken을 인증할 경우, 이 provider의 authenticate() 메소드를 호출하게 된다.
    return isAssignable(JwtAuthenticationToken.class, authentication); // 형변환 가능한지 체크
  }

}