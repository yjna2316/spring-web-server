package com.study.spring.webserver.repository.post;

import com.study.spring.webserver.model.commons.Id;
import com.study.spring.webserver.model.post.Comment;
import com.study.spring.webserver.model.post.Post;

import java.util.List;
import java.util.Optional;

public interface CommentRepository {

  Comment insert(Comment comment);

  void update(Comment comment);

  Optional<Comment> findById(Id<Comment, Long> commentId);

  List<Comment> findAll(Id<Post, Long> postId);

}
