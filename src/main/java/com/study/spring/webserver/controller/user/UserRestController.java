package com.study.spring.webserver.controller.user;

import com.study.spring.webserver.controller.ApiResult;
import com.study.spring.webserver.error.NotFoundException;
import com.study.spring.webserver.model.user.Email;
import com.study.spring.webserver.model.user.Role;
import com.study.spring.webserver.model.user.User;
import com.study.spring.webserver.security.Jwt;
import com.study.spring.webserver.security.JwtAuthentication;
import com.study.spring.webserver.service.user.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static com.study.spring.webserver.controller.ApiResult.OK;
import static java.util.stream.Collectors.toList;

@RestController
@RequestMapping("api")
@Api(tags = "사용자 APIs")
public class UserRestController {

  private final Jwt jwt;

  private final UserService userService;

  public UserRestController(Jwt jwt, UserService userService) {
    this.jwt = jwt;
    this.userService = userService;
  }

  @PostMapping(path = "user/exists")
  @ApiOperation(value = "이메일 중복확인 (API 토큰 필요없음)")
  public ApiResult<Boolean> checkEmail(@RequestBody @ApiParam(value = "example: {\"address\": \"test00@gmail.com\"}") Map<String, String> request) {
    // 이메일 중복 확인
    // BODY 예시: {
    //	"email": "yjna2316@gmail.com"
    // }
    Email email = new Email(request.get("address"));
    return OK(userService.findByEmail(email).isPresent());
  }


  @PostMapping(path = "user/join")
  @ApiOperation(value = "회원가입 (API 토큰 필요없음)")
  public ApiResult<JoinResult> join(@RequestBody JoinRequest joinRequest) {
    User user = userService.join(
      joinRequest.getName(),
      new Email(joinRequest.getPrincipal()),
      joinRequest.getCredentials()
    );
    String apiToken = user.newApiToken(jwt, new String[]{Role.USER.value()});
    return OK(
      new JoinResult(apiToken, new UserDto(user))
    );
  }

  /**
   * @AuthenticationPrincipal
   *  - SecurityContextHolder.getContext().getAuthentication().getPrinciple() 과 동일
   *  - Object 타입 => id=Id[reference=User,value=4], name="yoonji", email=Email[address=yjna.dev@gmail.com]
   */
  @GetMapping(path = "user/me")
  @ApiOperation(value = "내 정보")
  public ApiResult<UserDto> me(@AuthenticationPrincipal JwtAuthentication authentication) {
    return OK(
      userService.findById(authentication.id)
        .map(UserDto::new)
        .orElseThrow(() -> new NotFoundException(User.class, authentication.id))
    );
  }


  @GetMapping(path = "user/connections")
  @ApiOperation(value = "내 친구 목록")
  public ApiResult<List<ConnectedUserDto>> connections(@AuthenticationPrincipal JwtAuthentication authentication) {
    return OK(
      userService.findAllConnectedUser(authentication.id).stream()
        .map(ConnectedUserDto::new)
        .collect(toList())
    );
  }
}

