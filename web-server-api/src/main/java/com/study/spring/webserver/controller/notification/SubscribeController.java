package com.study.spring.webserver.controller.notification;

import com.study.spring.webserver.controller.ApiResult;
import com.study.spring.webserver.message.PushSubscribedMessage;
import com.study.spring.webserver.security.JwtAuthentication;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.requestreply.RequestReplyFuture;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutionException;

import static com.study.spring.webserver.controller.ApiResult.OK;


@RestController
@RequestMapping("api")
@Api(tags = "Push 구독 APIs")
public class SubscribeController {

  private static final Logger logger = LoggerFactory.getLogger(SubscribeController.class);

  @Value("${spring.kafka.topic.subscription-request}")
  private String requestTopic;

  @Value("${spring.kafka.topic.subscription-reply}")
  private String requestReplyTopic;

  private final ReplyingKafkaTemplate<String, PushSubscribedMessage, PushSubscribedMessage> kafkaTemplate;

  public SubscribeController(ReplyingKafkaTemplate<String, PushSubscribedMessage, PushSubscribedMessage> kafkaTemplate) {
    this.kafkaTemplate = kafkaTemplate;
  }

  @PostMapping("/subscribe")
  @ApiOperation(value = "Push 구독")
  public ApiResult<PushSubscribedMessage> subscribe(@AuthenticationPrincipal JwtAuthentication authentication,
                                                    @RequestBody PushSubscribedMessage subscription) throws ExecutionException, InterruptedException {
    PushSubscribedMessage.PushSubscribedMessageBuilder subscriptionBuilder = new PushSubscribedMessage.PushSubscribedMessageBuilder(subscription);
    subscriptionBuilder.userId(authentication.id.value());

    ProducerRecord<String, PushSubscribedMessage> record = new ProducerRecord<>(requestTopic, subscriptionBuilder.build());
    record.headers().add(new RecordHeader(KafkaHeaders.REPLY_TOPIC, requestReplyTopic.getBytes()));

    RequestReplyFuture<String, PushSubscribedMessage, PushSubscribedMessage> sendAndReceive = kafkaTemplate.sendAndReceive(record);

    ConsumerRecord<String, PushSubscribedMessage> consumerRecord = sendAndReceive.get();

    logger.info("success to subscribe {}", consumerRecord.value());

    return OK(consumerRecord.value());
  }

}

/*
  @RestController
  @RequestMapping("api")
  @Api(tags = "Push 구독 APIs")
  public class SubscribeController {

    private final NotificationService notificationService;

    public SubscribeController(NotificationService notificationService) {
      this.notificationService = notificationService;
    }

    // 로그인을 하면 이 api 찔러서 subscribe를 한다
    @PostMapping("/subscribe")
    @ApiOperation(value = "Push 구독")
    public ApiResult<Subscription> subscribe(@AuthenticationPrincipal JwtAuthentication authentication,
                                             @RequestBody Subscription subscription) {
      Subscription.SubscriptionBuilder subscriptionBuilder = new Subscription.SubscriptionBuilder(subscription);
      subscriptionBuilder.userId(authentication.id);

      Subscription subscribe = notificationService.subscribe(subscriptionBuilder.build());
      return ApiResult.OK(subscribe);
    }
  }
*/
