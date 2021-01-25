package com.study.spring.webserver.service.user;

import com.google.common.eventbus.EventBus;
import com.study.spring.webserver.error.NotFoundException;
import com.study.spring.webserver.event.JoinEvent;
import com.study.spring.webserver.model.commons.Id;
import com.study.spring.webserver.model.user.ConnectedUser;
import com.study.spring.webserver.model.user.Email;
import com.study.spring.webserver.model.user.User;
import com.study.spring.webserver.repository.user.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

@Service
public class UserService {

  private final PasswordEncoder passwordEncoder;

  private final UserRepository userRepository;

  private final EventBus eventBus;

  public UserService(PasswordEncoder passwordEncoder, UserRepository userRepository, EventBus eventBus) {
    this.passwordEncoder = passwordEncoder;
    this.userRepository = userRepository;
    this.eventBus = eventBus;
  }

  @Transactional
  public User join(String name, Email email, String password) {
    checkArgument(isNotEmpty(password), "password must be provided.");
    checkArgument(
      password.length() >= 4 && password.length() <= 15,
      "password length must be between 4 and 15 characters."
    );

    User user = new User(name, email, passwordEncoder.encode(password));
    User saved = insert(user);

    // raise event // how? 해당 이벤트와 리스너 어떻게 연결하고 있지. 언제 연결하지?
    eventBus.post(new JoinEvent(saved));
    // eventBus에 유저 가입 이벤트를 발생시킨다.
    // JoinEvent가 버스로 쏴지면, JoinEventListener의 handleJoinEvent 메소드가 호출된다.

    // HTTP call to Push Service
    return saved;
  }

  @Transactional
  public User login(Email email, String password) {
    checkNotNull(password, "password must be provided.");

    User user = findByEmail(email)
      .orElseThrow(() -> new NotFoundException(User.class, email));
    user.login(passwordEncoder, password);
    user.afterLoginSuccess();
    update(user);
    return user;
  }

  @Transactional
  public User updateProfileImage(Id<User, Long> userId, String profileImageUrl) {
    User user = findById(userId)
      .orElseThrow(() -> new NotFoundException(User.class, userId));
    user.updateProfileImage(profileImageUrl);
    update(user);
    return user;
  }

  @Transactional(readOnly = true)
  public Optional<User> findById(Id<User, Long> userId) {
    checkNotNull(userId, "userId must be provided.");

    return userRepository.findById(userId);
  }

  @Transactional(readOnly = true)
  public Optional<User> findByEmail(Email email) {
    checkNotNull(email, "email must be provided.");

    return userRepository.findByEmail(email);
  }

  @Transactional(readOnly = true)
  public List<ConnectedUser> findAllConnectedUser(Id<User, Long> userId) {
    checkNotNull(userId, "userId must be provided.");

    return userRepository.findAllConnectedUser(userId);
  }

  @Transactional(readOnly = true)
  public List<Id<User, Long>> findConnectedIds(Id<User, Long> userId) {
    checkNotNull(userId, "userId must be provided.");

    return userRepository.findConnectedIds(userId);
  }

  private User insert(User user) {
    return userRepository.insert(user);
  }

  private void update(User user) {
    userRepository.update(user);
  }

}