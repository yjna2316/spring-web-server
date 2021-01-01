package com.study.spring.webserver.controller.user;

import com.study.spring.webserver.model.user.Email;
import com.study.spring.webserver.model.user.User;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.time.LocalDateTime;

public class UserDto {

  private final Long seq;

  private final Email email;

  private final String password;

  private final int loginCount;

  private final LocalDateTime lastLoginAt;

  private final LocalDateTime createAt;

  public UserDto(User user) {
    this.seq = user.getSeq();
    this.email = user.getEmail();
    this.createAt = user.getCreateAt();
    this.password = user.getPassword();
    this.loginCount = user.getLoginCount();
    this.lastLoginAt = user.getLastLoginAt().orElse(null);
  }

  public Long getUserId() {
    return seq;
  }

  public Email getEmail() {
    return email;
  }

  public String getPassword() {
    return password;
  }

  public int getLoginCount() {
    return loginCount;
  }

  public LocalDateTime getLastLoginAt() {
    return lastLoginAt;
  }

  public LocalDateTime getCreateAt() {
    return createAt;
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
      .append("seq", seq)
      .append("email", email)
      .append("loginCount", loginCount)
      .append("lastLoginAt", lastLoginAt)
      .append("createAt", createAt)
      .toString();
  }
}
