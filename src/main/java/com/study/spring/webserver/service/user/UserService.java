package com.study.spring.webserver.service.user;

import com.study.spring.webserver.error.DuplicateEmailException;
import com.study.spring.webserver.model.commons.Id;
import com.study.spring.webserver.model.user.Email;
import com.study.spring.webserver.model.user.User;
import com.study.spring.webserver.repository.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

@Service
public class UserService {

  private final UserRepository userRepository;

  public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Transactional
  public User join(Email email, String password) {
    checkArgument(isNotEmpty(password), "password must be provided");
    checkArgument(
      password.length() >= 4 && password.length() <= 15,
      "password length must be between 4 and 15 characters."
    );
    validateDuplicateEmail(email);
    User user = new User(email, password);
    return insert(user);
  }

  @Transactional
  public List<User> findAll() {
    return userRepository.findAll();
  }

  @Transactional(readOnly = true)
  public Optional<User> findById(Long userId) {
    checkNotNull(userId, "userId must be provided");

    return userRepository.findById(Id.of(User.class, userId));
  }

  @Transactional(readOnly = true)
  public Optional<User> findByEmail(Email email) {
    checkNotNull(email, "email must be provided");

    return userRepository.findByEmail(email);
  }

  private User insert(User user) {
    return userRepository.insert(user);
  }

  private void update(User user) {
    userRepository.update(user);
  }

  private void validateDuplicateEmail(Email email) {
    userRepository.findByEmail(email).ifPresent(e -> { throw new DuplicateEmailException(); });
  }
}
