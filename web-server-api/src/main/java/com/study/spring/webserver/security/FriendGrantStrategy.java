package com.study.spring.webserver.security;

import com.study.spring.webserver.model.commons.Id;
import com.study.spring.webserver.model.user.User;
import com.study.spring.webserver.service.user.UserService;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
public class FriendGrantStrategy implements GrantStrategy {

  private UserService userService;

  public FriendGrantStrategy(UserService userService) {
    this.userService = userService;
  }

  @Override
  public boolean grant(Id<User, Long> me, Id<User, Long> target) {
    List<Id<User, Long>> connectedIds = userService.findConnectedIds(me);
    return connectedIds.contains(target);
  }
}
