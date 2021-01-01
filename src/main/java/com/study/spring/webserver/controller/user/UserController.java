package com.study.spring.webserver.controller.user;

import com.study.spring.webserver.controller.ApiResult;
import com.study.spring.webserver.error.NotFoundException;
import com.study.spring.webserver.model.user.Email;
import com.study.spring.webserver.model.user.User;
import com.study.spring.webserver.service.user.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static com.study.spring.webserver.controller.ApiResult.OK;
import static java.util.stream.Collectors.toList;

@RestController
@RequestMapping("api")
public class UserController {

  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  @PostMapping(path = "user/join")
    public ApiResult<UserDto> join(@RequestBody JoinRequest joinRequest) {
    User user = userService.join(new Email(joinRequest.getPrincipal()), joinRequest.getCredentials());
    return OK(
      new UserDto(user)
    );
  }


  @GetMapping("user/{userId}")
  public ApiResult<UserDto> findById(@PathVariable("userId") Long userId) {
    return OK(
      userService.findById(userId)
        .map(UserDto::new)
        .orElseThrow(NotFoundException::new)
    );
  }

  @GetMapping(path = "user/list")
  public ApiResult<List> userList() {
    return OK(
      userService.findAll().stream()
        .map(UserDto::new)
        .collect(toList())
    );
  }
}

