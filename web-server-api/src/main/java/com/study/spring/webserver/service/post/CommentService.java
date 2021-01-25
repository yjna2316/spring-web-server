package com.study.spring.webserver.service.post;

import com.google.common.eventbus.EventBus;
import com.study.spring.webserver.error.NotFoundException;
import com.study.spring.webserver.event.CommentCreatedEvent;
import com.study.spring.webserver.model.commons.Id;
import com.study.spring.webserver.model.post.Post;
import com.study.spring.webserver.model.user.User;
import com.study.spring.webserver.repository.post.PostRepository;
import com.study.spring.webserver.model.post.Comment;
import com.study.spring.webserver.repository.post.CommentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Collections.emptyList;

@Service
public class CommentService {

  private final EventBus eventBus;

  private final PostRepository postRepository;

  private final CommentRepository commentRepository;

  public CommentService(EventBus eventBus, PostRepository postRepository, CommentRepository commentRepository) {
    this.eventBus = eventBus;
    this.postRepository = postRepository;
    this.commentRepository = commentRepository;
  }

  @Transactional
  public Comment write(Id<Post, Long> postId, Id<User, Long> postWriterId, Id<User, Long> userId, Comment comment) {
    checkArgument(comment.getPostId().equals(postId), "comment.postId must equals postId");
    checkArgument(comment.getUserId().equals(userId), "comment.userId must equals userId");
    checkNotNull(comment, "comment must be provided.");

    return findPost(postId, postWriterId, userId)
      .map(post -> {
        post.incrementAndGetComments();
        postRepository.update(post);
        Comment inserted = insert(comment);

        // raise event
        eventBus.post(new CommentCreatedEvent(inserted));

        return inserted;
      })
      .orElseThrow(() -> new NotFoundException(Post.class, postId, userId));
  }

  @Transactional(readOnly = true)
  public List<Comment> findAll(Id<Post, Long> postId, Id<User, Long> postWriterId, Id<User, Long> userId) {
    // 존재하지 않는 포스트의 댓글을 조회하는 경우일 수 있으므로, findPost를 통해 해당 포스트 정보를 먼저 조회한다. null이면 emptyList 반환
    return findPost(postId, postWriterId, userId)
      .map(post -> commentRepository.findAll(postId))
      .orElse(emptyList());
  }

  private Optional<Post> findPost(Id<Post, Long> postId, Id<User, Long> postWriterId, Id<User, Long> userId) {
    checkNotNull(postId, "postId must be provided.");
    checkNotNull(postWriterId, "postWriterId must be provided.");
    checkNotNull(userId, "userId must be provided.");

    return postRepository.findById(postId, postWriterId, userId);
  }

  private Comment insert(Comment comment) {
    return commentRepository.insert(comment);
  }

  private void update(Comment comment) {
    commentRepository.update(comment);
  }

}