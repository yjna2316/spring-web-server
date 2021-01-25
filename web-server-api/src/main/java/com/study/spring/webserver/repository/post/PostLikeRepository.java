package com.study.spring.webserver.repository.post;

import com.study.spring.webserver.model.commons.Id;
import com.study.spring.webserver.model.post.Post;
import com.study.spring.webserver.model.user.User;

public interface PostLikeRepository {
  void insert(Id<User, Long> userId, Id<Post, Long> postId);
}

