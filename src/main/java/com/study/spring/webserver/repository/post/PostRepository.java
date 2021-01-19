package com.study.spring.webserver.repository.post;

import com.study.spring.webserver.model.commons.Id;
import com.study.spring.webserver.model.post.Post;
import com.study.spring.webserver.model.user.User;

import java.util.List;
import java.util.Optional;

public interface PostRepository {

  Post insert(Post post);

  void update(Post post);

  Optional<Post> findById(Id<Post, Long> postId);

  List<Post> findAll(Id<User, Long> userId);

}
