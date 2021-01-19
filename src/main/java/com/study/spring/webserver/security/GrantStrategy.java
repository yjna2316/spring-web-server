package com.study.spring.webserver.security;

import com.study.spring.webserver.model.commons.Id;
import com.study.spring.webserver.model.user.User;

public interface GrantStrategy {

  boolean grant(Id<User, Long> me, Id<User, Long> target);

}
