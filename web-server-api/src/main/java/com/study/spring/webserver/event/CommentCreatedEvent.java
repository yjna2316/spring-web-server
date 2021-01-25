package com.study.spring.webserver.event;

import com.study.spring.webserver.error.NotFoundException;
import com.study.spring.webserver.model.commons.Id;
import com.study.spring.webserver.model.post.Comment;
import com.study.spring.webserver.model.post.Post;
import com.study.spring.webserver.model.post.Writer;
import com.study.spring.webserver.model.user.User;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class CommentCreatedEvent {

  private final Id<Post, Long> postId;

  private final Id<User, Long> userId;

  private final Writer commentWriter;

  private final Id<Comment, Long> commentId;

  public CommentCreatedEvent(Comment comment) {
    this.postId = comment.getPostId();
    this.userId = comment.getUserId();
    this.commentWriter = comment.getWriter().orElseThrow(() -> new NotFoundException("Writer should be exist"));
    this.commentId = Id.of(Comment.class, comment.getSeq());
  }

  public Id<Post, Long> getPostId() {
    return postId;
  }

  public Id<User, Long> getUserId() {
    return userId;
  }

  public Writer getCommentWriter() {
    return commentWriter;
  }

  public Id<Comment, Long> getCommentId() {
    return commentId;
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
      .append("postId", postId)
      .append("userId", userId)
      .append("commentId", commentId)
      .append("commentWriter", commentWriter)
      .toString();
  }

}
