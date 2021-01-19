package com.study.spring.webserver.model.user;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.time.LocalDateTime;

import static com.google.common.base.Preconditions.checkNotNull;

public class ConnectedUser {
  private final Long seq;

  private final String name;

  private final Email email;

  private final LocalDateTime grantedAt;

  public ConnectedUser(Long seq, String name, Email email, LocalDateTime grantedAt) {
    checkNotNull(seq, "seq must be provided.");
    checkNotNull(name, "name must be provided.");
    checkNotNull(email, "email must be provided.");
    checkNotNull(grantedAt, "grantedAt must be provided.");

    this.seq = seq;
    this.name = name;
    this.email = email;
    this.grantedAt = grantedAt;
  }

  public Long getSeq() {
    return seq;
  }

  public String getName() {
    return name;
  }

  public Email getEmail() {
    return email;
  }

  public LocalDateTime getGrantedAt() {
    return grantedAt;
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
      .append("seq", seq)
      .append("name", name)
      .append("email", email)
      .append("grantedAt", grantedAt)
      .toString();
  }
}
