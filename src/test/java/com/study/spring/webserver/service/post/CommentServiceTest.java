package com.study.spring.webserver.service.post;

import com.study.spring.webserver.error.NotFoundException;
import com.study.spring.webserver.model.commons.Id;
import com.study.spring.webserver.model.post.Comment;
import com.study.spring.webserver.model.post.Post;
import com.study.spring.webserver.model.post.Writer;
import com.study.spring.webserver.model.user.Email;
import com.study.spring.webserver.model.user.User;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CommentServiceTest {

  private final Logger log = LoggerFactory.getLogger(getClass());

  @Autowired
  private PostService postService;

  @Autowired private CommentService commentService;

  private Id<Post, Long> postId;

  private Id<User, Long> postWriterId;

  private Id<User, Long> userId;

  @BeforeAll
  void setUp() {
    postId = Id.of(Post.class, 1L);
    postWriterId = Id.of(User.class, 1L);
    userId = Id.of(User.class, 2L);
  }

  @Test
  @Order(1)
  void 본인_포스트에_코멘트를_작성한다() {
    String contents = randomAlphabetic(40);
    Post beforePost = postService.findById(postId, postWriterId, userId).orElseThrow(() -> new NotFoundException(Post.class, postId));
    Comment comment = commentService.write(
      postId,
      postWriterId,
      userId,
      new Comment(userId, postId, new Writer(new Email("user@gmail.com"), "user"), contents)
    );
    Post afterPost = postService.findById(postId, postWriterId, userId).orElseThrow(() -> new NotFoundException(Post.class, postId));
    // comment 추가 후 post.comments 카운트가 이전과 비교해서 1증가했는지 확인
    assertThat(afterPost.getComments() - beforePost.getComments(), is(1));
    assertThat(comment, is(notNullValue()));
    assertThat(comment.getSeq(), is(notNullValue()));
    assertThat(comment.getContents(), is(contents));
    assertThat(comment.getPostId(), is(postId));
    assertThat(comment.getUserId(), is(userId));
    log.info("Written comment: {}", comment);
  }

  @Test
  @Order(2)
  void 댓글_목록을_조회한다() {
    // 내가 단 댓글과 친구가 단 댓글 모두 조회된다.
    List<Comment> comments = commentService.findAll(postId, postWriterId, userId);
    assertThat(comments, is(notNullValue()));
    assertThat(comments.size(), is(2));
  }

  @Test
  @Order(3)
  void 존재하지_않는_포스트에_코멘트를_달면_예외가_발한다() {
    String content = "this is user, my post";
    Writer commentWriterMe = new Writer(new Email("user@gmail.com"), "user");

    Id<Post, Long> invalidPostId = Id.of(Post.class, 100L);

    assertThrows(NotFoundException.class, () -> commentService.write(invalidPostId, postWriterId, userId, new Comment(userId, invalidPostId, commentWriterMe, content)));
  }
}
