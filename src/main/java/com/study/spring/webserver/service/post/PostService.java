package com.study.spring.webserver.service.post;

import com.study.spring.webserver.error.NotFoundException;
import com.study.spring.webserver.model.commons.Id;
import com.study.spring.webserver.model.post.Post;
import com.study.spring.webserver.model.user.User;
import com.study.spring.webserver.repository.post.PostRepository;
import com.study.spring.webserver.repository.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

@Service
public class PostService {

  private final UserRepository userRepository;

  private final PostRepository postRepository;

  public PostService(UserRepository userRepository, PostRepository postRepository) {
    this.userRepository = userRepository;
    this.postRepository = postRepository;
  }

  @Transactional
  public Post write(Post post) {
    return insert(post);
  }

  @Transactional
  public Post modify(Post post) {
    update(post);
    return post;
  }

  @Transactional(readOnly = true)
  public Optional<Post> findById(Id<Post, Long> postId) {
    checkNotNull(postId, "postId must be provided.");

    return postRepository.findById(postId);
  }

  @Transactional(readOnly = true)
  public List<Post> findAll(Id<User, Long> userId) {
    checkNotNull(userId, "userId must be provided.");

    userRepository.findById(userId)
      .orElseThrow(() -> new NotFoundException(User.class, userId));
    return postRepository.findAll(userId);
  }

  private Post insert(Post post) {
    return postRepository.insert(post);
  }

  private void update(Post post) {
    postRepository.update(post);
  }

}
