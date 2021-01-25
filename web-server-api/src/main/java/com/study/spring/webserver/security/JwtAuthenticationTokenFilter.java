package com.study.spring.webserver.security;

import com.study.spring.webserver.model.user.Email;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.toList;

/**
 * 필터 체인에서 UsernamePasswordAuthenticationFilter 앞에 위치
 * HTTP 요청 헤더에서 JWT 추출하고 유효성 검증
 * JWT가 유효하다면, 사용자 정보(JwtAuthentication, JwtAuthenticationToken 타입)를 생성하고 SecurityContextHolder에 저장함
 */

public class JwtAuthenticationTokenFilter extends GenericFilterBean {

  private static final Pattern BEARER = Pattern.compile("^Bearer$", Pattern.CASE_INSENSITIVE);

  private final Logger log = LoggerFactory.getLogger(getClass());

  private final String headerKey;

  private final Jwt jwt;

  public JwtAuthenticationTokenFilter(String headerKey, Jwt jwt) {
    this.headerKey = headerKey;
    this.jwt = jwt;
  }

  @Override
  public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
    throws IOException, ServletException {
    HttpServletRequest request = (HttpServletRequest) req;
    HttpServletResponse response = (HttpServletResponse) res;

    if (SecurityContextHolder.getContext().getAuthentication() == null) {
      String authorizationToken = obtainAuthorizationToken(request);
      if (authorizationToken != null) {
        try {
          Jwt.Claims claims = verify(authorizationToken);
          log.debug("Jwt parse result: {}", claims);

          // 만료 10분 전에 토큰을 갱신해서 보내준다
          if (canRefresh(claims, 6000 * 10)) {
            String refreshedToken = jwt.refreshToken(authorizationToken);
            response.setHeader(headerKey, refreshedToken);
          }

          Long userKey = claims.userKey;
          Email email = claims.email;
          String name = claims.name;

          List<GrantedAuthority> authorities = obtainAuthorities(claims);

          if (userKey != null && email != null && name != null && authorities.size() > 0) {
            JwtAuthenticationToken authentication =
              new JwtAuthenticationToken(new JwtAuthentication(userKey, email, name), null, authorities);
            // LoginForm에서 허용된 기본 username과 password 외에 사용자가 정의한 입력 필드를 추가하고 이를 파라미터로 넘기기
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
          }
        } catch (Exception e) {
          log.warn("Jwt processing failed: {}", e.getMessage());
        }
      }
    } else {
      log.debug("SecurityContextHolder not populated with security token, as it already contained: '{}'",
        SecurityContextHolder.getContext().getAuthentication());
    }

    chain.doFilter(request, response); // 다음 필터에 request, response를 넘겨준다.
  }

  private boolean canRefresh(Jwt.Claims claims, long refreshRangeMillis) {
    long exp = claims.exp();
    if (exp > 0) {
      long remain = exp - System.currentTimeMillis();
      return remain < refreshRangeMillis;
    }
    return false;
  }

  private List<GrantedAuthority> obtainAuthorities(Jwt.Claims claims) {
    String[] roles = claims.roles;
    return roles == null || roles.length == 0 ?
      Collections.emptyList() :
      Arrays.stream(roles).map(SimpleGrantedAuthority::new).collect(toList());
  }

  private String obtainAuthorizationToken(HttpServletRequest request) {
    String token = request.getHeader(headerKey);
    if (token != null) {
      if (log.isDebugEnabled())
        log.debug("Jwt authorization api detected: {}", token);
      try {
        token = URLDecoder.decode(token, "UTF-8");
        String[] parts = token.split(" ");
        if (parts.length == 2) {
          String scheme = parts[0];
          String credentials = parts[1];
          return BEARER.matcher(scheme).matches() ? credentials : null;
        }
      } catch (UnsupportedEncodingException e) {
        log.error(e.getMessage(), e);
      }
    }

    return null;
  }

  private Jwt.Claims verify(String token) {
    return jwt.verify(token);
  }

}