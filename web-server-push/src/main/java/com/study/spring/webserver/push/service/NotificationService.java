package com.study.spring.webserver.push.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.spring.webserver.controller.user.UserDto;
import com.study.spring.webserver.message.CommentCreatedMessage;
import com.study.spring.webserver.message.PushSubscribedMessage;
import com.study.spring.webserver.message.UserJoinedMessage;
import com.study.spring.webserver.model.user.User;
import com.study.spring.webserver.push.model.PushMessage;
import com.study.spring.webserver.push.model.Subscription;
import com.study.spring.webserver.push.repository.SubscriptionRepository;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class NotificationService {

  private final Logger log = LoggerFactory.getLogger(NotificationService.class);

  private final PushService pushService;

  private final ObjectMapper objectMapper;

  private final SubscriptionRepository subscriptionRepository;

  public NotificationService(
    PushService pushService,
    ObjectMapper objectMapper,
    SubscriptionRepository subscriptionRepository
  ) {
    this.pushService = pushService;
    this.objectMapper = objectMapper;
    this.subscriptionRepository = subscriptionRepository;
  }


/*
  public Subscription subscribe(Subscription subscription) {
    return subscriptionRepository.save(subscription);
  }
*/

  @KafkaListener(topics = "${spring.kafka.topic.subscription-request}",
    containerFactory = "kafkaListenerContainerSubscriptionFactory")
  @SendTo
  public PushSubscribedMessage subscribe(PushSubscribedMessage pushSubscribedMessage) {
    Subscription saved = subscriptionRepository.save(Subscription.of(pushSubscribedMessage));
    return saved.toMessage();
  }

  @KafkaListener(topics = "${spring.kafka.topic.comment-created}",
    containerFactory = "kafkaListenerContainerPushMessageFactory")
  public void notifyPostWriter(CommentCreatedMessage commentCreatedMessage) throws Exception {
    UserDto postWriter = commentCreatedMessage.getPostWriter();
    // 자기 자신에게는 안보낸다.
    if (postWriter.getSeq().equals(commentCreatedMessage.getUserId())) {
      return;
    }

    Long targetUserSeq = postWriter.getSeq();

    Optional<Subscription> maybeSubscription = subscriptionRepository.findByUserSeq(targetUserSeq);

    if (!maybeSubscription.isPresent()) {
      log.warn("Can not send message to user {} because not found any subscription", targetUserSeq);
      return;
    }

    PushMessage pushMessage = new PushMessage(
      commentCreatedMessage.getCommentWriter().getName().orElse("유저") + "이 댓글을 달았어요.",
      "/",
      "한번 확인해 보세요."
    );

    Subscription subscription = maybeSubscription.get();
    log.info("{} -> {} 푸쉬발송", subscription.getNotificationEndPoint(), pushMessage.getMessage());

    Notification notification = new Notification(
      subscription.getNotificationEndPoint(),
      subscription.getPublicKey(),
      subscription.getAuth(),
      objectMapper.writeValueAsBytes(pushMessage));

    pushService.send(notification);
  }


  @KafkaListener(topics = "${spring.kafka.topic.user-joined}",
    containerFactory = "kafkaListenerContainerPushMessageFactory")
  public void notifyAll(UserJoinedMessage userJoinedMessage) throws Exception {
    String name = userJoinedMessage.getName();
    Long userId = userJoinedMessage.getUserId();

    PushMessage message = new PushMessage(
      name + " Joined!",
      "/friends/" + userId,
      "Please send welcome message"
    );
    List<Subscription> subscriptions = subscriptionRepository.findAll();

    for (Subscription subscription : subscriptions) {
      Notification notification = new Notification(
        subscription.getNotificationEndPoint(),
        subscription.getPublicKey(),
        subscription.getAuth(),
        objectMapper.writeValueAsBytes(message));
      pushService.send(notification);
    }
  }


  /*
  public PushMessage notifyAll(PushMessage message) throws Exception {
    List<Subscription> subscriptions = subscriptionRepository.findAll();
    for (Subscription subscription : subscriptions) {
      Notification notification = new Notification(
        subscription.getNotificationEndPoint(),
        subscription.getPublicKey(),
        subscription.getAuth(),
        objectMapper.writeValueAsBytes(message));
      pushService.send(notification);
    }

    return message;
  }
  */


  public PushMessage notifyUser(User user, PushMessage message) throws Exception {
    Optional<Subscription> maybeSubscription = subscriptionRepository.findByUserSeq(user.getSeq());

    if (!maybeSubscription.isPresent()) {
      log.info("Can not send message to user {} because not found any subscription", user);
      return null;
    }

    Subscription subscription = maybeSubscription.get();
    log.info("TRY TO SEND {} to SUBSCRIPTION {}", message, subscription);

    Notification notification = new Notification(
      subscription.getNotificationEndPoint(),
      subscription.getPublicKey(),
      subscription.getAuth(),
      objectMapper.writeValueAsBytes(message));

    pushService.send(notification);

    return message;
  }

}
