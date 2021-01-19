package com.study.spring.webserver.repository.post;

import com.study.spring.webserver.model.post.PostLike;
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
  public PostLike insert(PostLike postLike) {
    KeyHolder keyHolder = new GeneratedKeyHolder();
    jdbcTemplate.update(conn -> {
      PreparedStatement ps = conn.prepareStatement("INSERT INTO likes(seq,user_seq,post_seq,create_at) VALUES (null,?,?,?)", new String[]{"seq"});
      ps.setLong(1, postLike.getUserId().value());
      ps.setLong(2, postLike.getPostId().value());
      ps.setTimestamp(3, timestampOf(postLike.getCreateAt()));
      return ps;
    }, keyHolder);

    Number key = keyHolder.getKey();
    long generatedSeq = key != null ? key.longValue() : -1;
    return new PostLike.Builder(postLike)
      .seq(generatedSeq)
      .build();
  }
}
