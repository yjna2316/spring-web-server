package com.study.spring.webserver.controller.post;

import com.study.spring.webserver.model.post.Comment;
import com.study.spring.webserver.model.post.Writer;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.time.LocalDateTime;

import static org.springframework.beans.BeanUtils.copyProperties;

public class CommentDto {

  private Long seq;

  private String contents;

  private Writer writer;

  private LocalDateTime createAt;

  public CommentDto(Comment source) {
    copyProperties(source, this);

    this.writer = source.getWriter().orElse(null);

  }

  public Long getSeq() {
    return seq;
  }

  public void setSeq(Long seq) {
    this.seq = seq;
  }

  public String getContents() {
    return contents;
  }

  public void setContents(String contents) {
    this.contents = contents;
  }

  public Writer getWriter() {
    return writer;
  }

  public void setWriter(Writer writer) {
    this.writer = writer;
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
      .append("contents", contents)
      .append("writer", writer)
      .append("createAt", createAt)
      .toString();
  }

}