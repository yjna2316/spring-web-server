package com.study.spring.webserver.controller.user;

import com.study.spring.webserver.controller.ApiResult;
import com.study.spring.webserver.error.NotFoundException;
import com.study.spring.webserver.model.user.Email;
import com.study.spring.webserver.model.user.Role;
import com.study.spring.webserver.model.user.User;
import com.study.spring.webserver.security.Jwt;
import com.study.spring.webserver.security.JwtAuthentication;
import com.study.spring.webserver.service.user.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static com.study.spring.webserver.controller.ApiResult.OK;
import static java.util.stream.Collectors.toList;

@RestController
@RequestMapping("api")
public class UserRestController {

  private final Jwt jwt;

  private final UserService userService;

  public UserRestController(Jwt jwt, UserService userService) {
    this.jwt = jwt;
    this.userService = userService;
  }

  @PostMapping(path = "user/exists")
  public ApiResult<Boolean> checkEmail(@RequestBody Map<String, Email> request) { // 원소 1개 밖에 없어서 DTO 대신 Map 사용
    // 이메일 중복 확인
    // BODY 예시: {
    //	"email": "yjna2316@gmail.com"
    // }
    Email userEmail = request.get("email");
    return userService.findByEmail(userEmail).isPresent() ?  OK(true) : OK(false);
  }


  @PostMapping(path = "user/join")
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
   * @AuthenticationPrincipal: securityContextHolder에서 principal 부분만 꺼내옴.
   * SecurityContextHolder.getContext().getAuthentication().getPrinciple()과 동일
   */
  @GetMapping(path = "user/me")
  public ApiResult<UserDto> me(@AuthenticationPrincipal JwtAuthentication authentication) {
    return OK(
      userService.findById(authentication.id)
        .map(UserDto::new)
        .orElseThrow(() -> new NotFoundException(User.class, authentication.id))
    );
  }


  @GetMapping(path = "user/connections")
  public ApiResult<List<ConnectedUserDto>> connections(@AuthenticationPrincipal JwtAuthentication authentication) {
    return OK(
      userService.findAllConnectedUser(authentication.id).stream()
        .map(ConnectedUserDto::new)
        .collect(toList())
    );
  }
}

