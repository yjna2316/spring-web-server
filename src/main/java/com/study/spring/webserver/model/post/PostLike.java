package com.study.spring.webserver.model.post;

import com.study.spring.webserver.model.commons.Id;
import com.study.spring.webserver.model.user.User;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.time.LocalDateTime;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.time.LocalDateTime.now;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

public class PostLike {

  private final Long seq;

  private final Id<User, Long> userId;

  private final Id<Post, Long> postId;

  private final LocalDateTime createAt;

  public PostLike(Id<User, Long> userId, Id<Post, Long> postId) {
    this(null, userId, postId, null);
  }

  public PostLike(Long seq, Id<User, Long> userId, Id<Post, Long> postId, LocalDateTime createAt) {
    checkNotNull(userId, "userId must be provided");
    checkNotNull(postId, "postId must be provided");

    this.seq = seq;
    this.userId = userId;
    this.postId = postId;
    this.createAt = defaultIfNull(createAt, now());
  }

  public Id<User, Long> getUserId() {
    return userId;
  }

  public Id<Post, Long> getPostId() {
    return postId;
  }

  public LocalDateTime getCreateAt() {
    return createAt;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;

    if (o == null || getClass() != o.getClass()) return false;

    PostLike postLike = (PostLike) o;

    return Objects.equals(seq, postLike.seq);
  }

  @Override
  public int hashCode() {
    return Objects.hash(seq);
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
      .append("seq", seq)
      .append("userId", userId)
      .append("postId", postId)
      .append("createdAt", createAt)
      .toString();
  }

  static public class Builder {
    private Long seq;
    private Id<User, Long> userId;
    private Id<Post, Long> postId;
    private LocalDateTime createAt;


    public Builder() {
    }

    public Builder(PostLike postLike) {
      this.seq = postLike.seq;
      this.userId = postLike.userId;
      this.postId = postLike.postId;
      this.createAt = postLike.createAt;
    }

    public Builder seq(Long seq) {
      this.seq = seq;
      return this;
    }

    public Builder userId(Id<User, Long> userId) {
      this.userId = userId;
      return this;
    }

    public Builder postId(Id<Post, Long> postId) {
      this.postId = postId;
      return this;
    }

    public Builder createAt(LocalDateTime createAt) {
      this.createAt = createAt;
      return this;
    }

    public PostLike build() {
      return new PostLike(seq, userId, postId, createAt);
    }
  }
}
