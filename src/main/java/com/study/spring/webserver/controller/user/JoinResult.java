package com.study.spring.webserver.controller.user;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import static com.google.common.base.Preconditions.checkNotNull;

public class JoinResult {

  private final String apiToken;

  private final UserDto user;

  public JoinResult(String apiToken, UserDto user) {
    checkNotNull(apiToken, "apiToken must be provided.");
    checkNotNull(user, "user must be provided.");

    this.apiToken = apiToken;
    this.user = user;
  }

  public String getApiToken() {
    return apiToken;
  }

  public UserDto getUser() {
    return user;
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
      .append("apiToken", apiToken)
      .append("user", user)
      .toString();
  }

}