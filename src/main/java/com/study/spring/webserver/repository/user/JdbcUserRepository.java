package com.study.spring.webserver.repository.user;

import com.study.spring.webserver.model.commons.Id;
import com.study.spring.webserver.model.user.Email;
import com.study.spring.webserver.model.user.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;

import static com.study.spring.webserver.util.DateTimeUtils.dateTimeOf;
import static com.study.spring.webserver.util.DateTimeUtils.timestampOf;
import static java.util.Optional.ofNullable;

@Repository
public class JdbcUserRepository implements UserRepository {

  private final JdbcTemplate jdbcTemplate;

  /*
   * JdbcTemplate 주입은 외부 어디서 언제 해주고,  DataSource 정보는 properties에서 어떻게 일어오는거지?
   * => 스프링부트의 auto-configuration에 의해서
   *  https://dzone.com/articles/how-springboot-autoconfiguration-magic-works
   **/
  public JdbcUserRepository(JdbcTemplate jdbcTemplate) { // ? DataSource 따로 선언 안해줬는데 어디서 가져오는거지?  properties에서 스프링이 알아서 읽어오나?
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  public User insert(User user) {
    KeyHolder keyHolder = new GeneratedKeyHolder();
    jdbcTemplate.update(conn -> {
      // TODO 이름 프로퍼티 처리
      PreparedStatement ps = conn.prepareStatement("INSERT INTO users(seq,email,passwd,login_count,last_login_at,create_at) VALUES (null,?,?,?,?,?)", new String[]{"seq"});
      ps.setString(1, user.getEmail().getAddress());
      ps.setString(2, user.getPassword());
      ps.setInt(3, user.getLoginCount());
      ps.setTimestamp(4, timestampOf(user.getLastLoginAt().orElse(null)));
      ps.setTimestamp(5, timestampOf(user.getCreateAt()));
      return ps;
    }, keyHolder);

    Number key = keyHolder.getKey();
    long generatedSeq = key != null ? key.longValue() : -1;
    return new User.Builder(user)
      .seq(generatedSeq)
      .build();
  }

  @Override
  public void update(User user) {
    // TODO 이름 프로퍼티 처리
    jdbcTemplate.update("UPDATE users SET passwd=?,login_count=?,last_login_at=? WHERE seq=?",
      user.getPassword(),
      user.getLoginCount(),
      user.getLastLoginAt().orElse(null),
      user.getSeq()
    );
  }

  @Override
  public List<User> findAll() {
    return jdbcTemplate.query("SELECT * FROM users", mapper);
  }

  @Override
  public Optional<User> findById(Id<User, Long> userId) {
    List<User> user = jdbcTemplate.query("SELECT * FROM users WHERE seq=?",
      new Object[]{userId.value()},
      mapper
    );
//    return ofNullable(users.isEmpty() ? null : users.get(0));
    return user.stream().findAny();
  }

  @Override
  public Optional<User> findByEmail(Email email) {
    List<User> user = jdbcTemplate.query("SELECT * FROM users WHERE email=?",
      new Object[]{email.getAddress()},
      mapper
    );
//    return ofNullable(users.isEmpty() ? null : users.get(0));
    return user.stream().findAny();
  }

  // static은 언제 사용할까? // 왜 static으로 해도 될까?
  /* DB에서 읽어온 결과(ResultSet)를 객체로 변환시켜주는 메소드 */
  static RowMapper<User> mapper = (rs, rowNum) -> new User.Builder()
    .seq(rs.getLong("seq"))
    .email(new Email(rs.getString("email")))
    .password(rs.getString("passwd"))
    .loginCount(rs.getInt("login_count"))
    .lastLoginAt(dateTimeOf(rs.getTimestamp("last_login_at")))
    .createAt(dateTimeOf(rs.getTimestamp("create_at")))
    .build();
}
