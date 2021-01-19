package com.study.spring.webserver.security;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * JwtAuthenticationToken
 * - 사용자 인증 전 또는 후 모두를 표현한다
 * - 인증 전이라면 인증정보(아이디/비밀번호)를 가지고 있고, authenticated는 false이다.
 * - 인증 후라면 사용자PK와 권한정보(ROLE_USER)를 가지고 있고, authenticated는 true이다.
 * - 스프링 시큐리티 기본 구현체 (UserPasswordAuthenticationToken)과 비슷한 역할을 한다.
 */
public class JwtAuthenticationToken extends AbstractAuthenticationToken {

  private final Object principal;

  private String credentials;

  // 로그인 전 (=인증 전) 수행
  public JwtAuthenticationToken(String principal, String credentials) {
    super(null);
    super.setAuthenticated(false); // 인증 되기 전이니까 false

    this.principal = principal;
    this.credentials = credentials;
  }

  // 로그인 후 (=인증 후) 수행
  JwtAuthenticationToken(Object principal, String credentials, Collection<? extends GrantedAuthority> authorities) {
    super(authorities);
    super.setAuthenticated(true);

    this.principal = principal;
    this.credentials = credentials;
  }

  AuthenticationRequest authenticationRequest() {
    return new AuthenticationRequest(String.valueOf(principal), credentials);
  }

  @Override
  public Object getPrincipal() {
    return principal;
  }

  @Override
  public String getCredentials() {
    return credentials;
  }

  public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
    if (isAuthenticated) {
      throw new IllegalArgumentException("Cannot set this token to trusted - use constructor which takes a GrantedAuthority list instead");
    }
    super.setAuthenticated(false);
  }

  @Override
  public void eraseCredentials() {
    super.eraseCredentials();
    credentials = null;
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
      .append("principal", principal)
      .append("credentials", "[PROTECTED]")
      .toString();
  }

}
