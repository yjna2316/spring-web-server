package com.study.spring.webserver.controller.user;

import com.study.spring.webserver.model.user.ConnectedUser;
import com.study.spring.webserver.model.user.Email;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.time.LocalDateTime;

import static org.springframework.beans.BeanUtils.copyProperties;

public class ConnectedUserDto {
  private Long seq;

  private Email email;

  private String name;

  private LocalDateTime grantedAt;

  public ConnectedUserDto(ConnectedUser source) {
    copyProperties(source, this);
  }

  public Long getSeq() {
    return seq;
  }

  public void setSeq(Long seq) {
    this.seq = seq;
  }

  public Email getEmail() {
    return email;
  }

  public String getName() {
    return name;
  }

  public void setEmail(Email email) {
    this.email = email;
  }

  public LocalDateTime getGrantedAt() {
    return grantedAt;
  }

  public void setGrantedAt(LocalDateTime grantedAt) {
    this.grantedAt = grantedAt;
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
      .append("seq", seq)
      .append("email", email)
      .append("name", name)
      .append("grantedAt", grantedAt)
      .toString();
  }

}
