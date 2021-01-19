package com.study.spring.webserver.controller.authentication;

import com.study.spring.webserver.controller.ApiResult;
import com.study.spring.webserver.error.UnauthorizedException;
import com.study.spring.webserver.security.AuthenticationRequest;
import com.study.spring.webserver.security.AuthenticationResult;
import com.study.spring.webserver.security.JwtAuthenticationToken;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.study.spring.webserver.controller.ApiResult.OK;

/**
 * AuthenticationRestController
 * - HTTP 요청에서 인증정보(아이디/비밀번호)를 추출함
 * - 추출한 인증정보로 JwtAuthenticationToken을 생성
 * - 사용자 인증을 위해 AuthenticationManager에게 JwtAuthenticationToken을 넘
 */
@RestController
@RequestMapping("api/auth")
@Api(tags = "인증 APIs")
public class AuthenticationRestController {

  private final AuthenticationManager authenticationManager;

  public AuthenticationRestController(AuthenticationManager authenticationManager) {
    this.authenticationManager = authenticationManager;
  }

  @PostMapping
  @ApiOperation(value = "사용자 로그인 (API 토큰 필요없음)")
  public ApiResult<AuthenticationResultDto> authentication(@RequestBody AuthenticationRequest authRequest) throws UnauthorizedException {
    try {
      // HTTP 요청에서 인증정보(사용자 ID, 비밀번호)를 추출해서 JwtAuthenticationToken을(Authentication 인터페이스 구현체) 생성한다.
      JwtAuthenticationToken authToken = new JwtAuthenticationToken(authRequest.getPrincipal(), authRequest.getCredentials());

      // AuthenticationManager에게 JwtAuthenticationToken을 넘겨 사용자 인증을 수행
      // Manager는 해당 authentication을 처리할 수 있는 provider를 찾아 던진다.
      Authentication authentication = authenticationManager.authenticate(authToken);

      // 인증 완료 후 생성된 인증 토큰은 contextHolder에 저장
      // 이후 실행되는 요청은 getAuthentication()해서 인증 토큰을 가져올 수 있게 된다.
      SecurityContextHolder.getContext().setAuthentication(authentication);
      return OK(
        new AuthenticationResultDto((AuthenticationResult) authentication.getDetails())
      );
    } catch (AuthenticationException e) {
      throw new UnauthorizedException(e.getMessage());
    }
  }
}