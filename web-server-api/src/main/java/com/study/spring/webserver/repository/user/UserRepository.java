package com.study.spring.webserver.repository.user;

import com.study.spring.webserver.model.commons.Id;
import com.study.spring.webserver.model.user.ConnectedUser;
import com.study.spring.webserver.model.user.Email;
import com.study.spring.webserver.model.user.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {

  User insert(User user);

  void update(User user);

  /**
   * 반환타입을 Optional한 이유
   * 공식 문서에 의하면, Optional은 Null값이 나오면 안되는 상황을 위해 만들어진 메소드 반환 타입이다.
   * 전체 유저를 조회할 경우에는 null이 허용되는 상황이므로 List를 사용하고,
   * Id나 Email로 특정 유저를 조회했을 때는 null 값이 나오면 안되는, 즉 에러가 발생할 수 있는 상황이므로 Optional로 반환한다.
   */
  Optional<User> findById(Id<User, Long> userId);

  Optional<User> findByEmail(Email email);

  List<ConnectedUser> findAllConnectedUser(Id<User, Long> userId);

  List<Id<User, Long>> findConnectedIds(Id<User, Long> userId);
}
