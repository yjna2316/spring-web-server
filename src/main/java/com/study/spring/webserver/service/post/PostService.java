package com.study.spring.webserver.service.post;

import com.study.spring.webserver.error.NotFoundException;
import com.study.spring.webserver.model.commons.Id;
import com.study.spring.webserver.model.post.Post;
import com.study.spring.webserver.model.post.PostLike;
import com.study.spring.webserver.model.user.User;
import com.study.spring.webserver.repository.post.PostLikeRepository;
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
  private final PostLikeRepository postLikeRepository;

  public PostService(UserRepository userRepository, PostRepository postRepository, PostLikeRepository postLikeRepository) {
    this.userRepository = userRepository;
    this.postRepository = postRepository;
    this.postLikeRepository = postLikeRepository;
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

  @Transactional
  public Optional<Post> like(Id<User, Long> userId, Id<Post, Long> postId, Id<User, Long> writerId) {
    return findById(userId, postId, writerId).map(post -> {
      if (!post.isLikesOfMe()) {
        post.incrementAndGetLikes();
        update(post);
        postLikeRepository.insert(new PostLike(userId, postId));
      }
      return post;
    });
  }

  @Transactional(readOnly = true)
  public Optional<Post> findById(Id<User, Long> userId, Id<Post, Long> postId, Id<User, Long> writerId) {
    checkNotNull(postId, "postId must be provided.");
    checkNotNull(writerId, "writerId must be provided.");
    {
      return postRepository.findById(userId, postId, writerId);
    }
  }

  @Transactional(readOnly = true)
  public List<Post> findAll(Id<User, Long> userId, Id<User, Long> writerId, long offset, int limit) {
    checkNotNull(userId, "userId must be provided.");
    checkNotNull(writerId, "writerId must be provided.");
    if (offset < 0)
      offset = 0;
    if (limit < 1 || limit > 5)
      limit = 5;

    userRepository.findById(writerId)
      .orElseThrow(() -> new NotFoundException(User.class, writerId));

    return postRepository.findAll(userId, writerId, offset, limit);
  }

  private Post insert(Post post) {
    return postRepository.insert(post);
  }

  private void update(Post post) {
    postRepository.update(post);
  }

}