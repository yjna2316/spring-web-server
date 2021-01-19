package com.study.spring.webserver.controller.user;

import com.study.spring.webserver.model.user.Email;
import com.study.spring.webserver.model.user.User;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.time.LocalDateTime;

import static org.springframework.beans.BeanUtils.copyProperties;

public class UserDto {

  private Long seq;

  private Email email;

  private String name;

  private int loginCount;

  private LocalDateTime lastLoginAt;

  private LocalDateTime createAt;

  public UserDto(User source) {
    // setter 메소드 대신 간결하게 코드를 작성하는 방법 - 필드 수가 많을 경우 굿
    // User 객체 내용을 UserDto 객체에 복사한다.
    copyProperties(source, this);

    this.lastLoginAt = source.getLastLoginAt().orElse(null);
  }

  public Long getSeq() {
    return seq;
  }

  public void setSeq(Long seq) {
    this.seq = seq;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Email getEmail() {
    return email;
  }

  public void setEmail(Email email) {
    this.email = email;
  }

  public int getLoginCount() {
    return loginCount;
  }

  public void setLoginCount(int loginCount) {
    this.loginCount = loginCount;
  }

  public LocalDateTime getLastLoginAt() {
    return lastLoginAt;
  }

  public void setLastLoginAt(LocalDateTime lastLoginAt) {
    this.lastLoginAt = lastLoginAt;
  }

  public LocalDateTime getCreateAt() {
    return createAt;
  }

  public void setCreateAt(LocalDateTime createAt) {
    this.createAt = createAt;
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
      .append("seq", seq)
      .append("name", name)
      .append("email", email)
      .append("loginCount", loginCount)
      .append("lastLoginAt", lastLoginAt)
      .append("createAt", createAt)
      .toString();
  }
}
