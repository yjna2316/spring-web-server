package com.study.spring.webserver.security;

import com.study.spring.webserver.model.commons.Id;
import com.study.spring.webserver.model.user.User;
import org.springframework.stereotype.Component;

@Component
public class SelfGrantStrategy implements GrantStrategy {
  @Override
  public boolean grant(Id<User, Long> me, Id<User, Long> target) {
    return me.equals(target);
  }
}
