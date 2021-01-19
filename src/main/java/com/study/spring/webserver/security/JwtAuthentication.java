package com.study.spring.webserver.security;

import com.study.spring.webserver.model.commons.Id;
import com.study.spring.webserver.model.user.Email;
import com.study.spring.webserver.model.user.User;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * JwtAuthentication
 * - 인증된 사용자 정보
 * - Controller에서 @AuthenticationPrincipal 어노테이션을 적용하면 SecurityContextHolder에서 Principal 부분만 꺼내옴
 */
public class JwtAuthentication {
  public final Id<User, Long> id;

  public final Email email;

  public final String name;

  JwtAuthentication(Long id, Email email, String name) {
    checkNotNull(id, "id must be provided.");
    checkNotNull(email, "email must be provided.");
    checkNotNull(name, "email must be provided.");

    this.id = Id.of(User.class, id);
    this.email = email;
    this.name = name;
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
      .append("id", id)
      .append("email", email)
      .append("name", name)
      .toString();
  }
}
