package com.study.spring.webserver.repository.post;

import com.study.spring.webserver.model.commons.Id;
import com.study.spring.webserver.model.post.Post;
import com.study.spring.webserver.model.post.PostLike;
import com.study.spring.webserver.model.user.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;

import static com.study.spring.webserver.util.DateTimeUtils.timestampOf;

@Repository
public class JdbcPostLikeRepository implements PostLikeRepository {

  private final JdbcTemplate jdbcTemplate;

  public JdbcPostLikeRepository(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  public void insert(Id<User, Long> userId, Id<Post, Long> postId) {
    jdbcTemplate.update(conn -> {
      PreparedStatement ps = conn.prepareStatement("INSERT INTO likes(seq,user_seq,post_seq) VALUES (null,?,?)");
      ps.setLong(1, userId.value());
      ps.setLong(2, postId.value());
      return ps;
    });
  }

}
