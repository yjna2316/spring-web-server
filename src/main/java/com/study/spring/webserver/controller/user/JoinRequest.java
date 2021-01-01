package com.study.spring.webserver.controller.user;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class JoinRequest {

  private String principal;

  private String credentials;

  protected JoinRequest() {}

  public JoinRequest(String principal, String credentials) {
    this.principal = principal;
    this.credentials = credentials;
  }

  public String getPrincipal() {
    return principal;
  }

  public String getCredentials() {
    return credentials;
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
      .append("principal", principal)
      .append("credentials", credentials)
      .toString();
  }

}