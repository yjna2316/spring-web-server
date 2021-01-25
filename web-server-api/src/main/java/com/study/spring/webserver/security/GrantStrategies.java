package com.study.spring.webserver.security;

import com.study.spring.webserver.model.commons.Id;
import com.study.spring.webserver.model.user.User;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

@Component
public class GrantStrategies {
  private final List<GrantStrategy> grantStrategies;

  public GrantStrategies(SelfGrantStrategy selfGrantStrategy, FriendGrantStrategy friendGrantStrategy) {
    checkNotNull(selfGrantStrategy, "selfGrantStrategy must be provided.");
    checkNotNull(friendGrantStrategy, "friendGrantStrategy must be provided.");

    this.grantStrategies = Arrays.asList(selfGrantStrategy, friendGrantStrategy);
  }

  public boolean grant(Id<User, Long> id, Id<User, Long> targetId) {
    return this.grantStrategies.stream()
      .anyMatch(userGrantStrategy -> userGrantStrategy.grant(id, targetId));
  }
}
