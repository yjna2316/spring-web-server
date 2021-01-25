package com.study.spring.webserver.event.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.study.spring.webserver.event.JoinEvent;
import com.study.spring.webserver.message.UserJoinedMessage;
import com.study.spring.webserver.model.commons.Id;
import com.study.spring.webserver.model.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;

public class JoinEventListener implements AutoCloseable {

  @Value("${spring.kafka.topic.user-joined}")
  private String userJoinedTopic;

  private final Logger log = LoggerFactory.getLogger(JoinEventListener.class);

  private final EventBus eventBus;

//  private final NotificationService notificationService;

  private final KafkaTemplate<String, UserJoinedMessage> kafkaTemplate;

  public JoinEventListener(EventBus eventBus, KafkaTemplate<String, UserJoinedMessage> kafkaTemplate) {
    this.eventBus = eventBus;
//    this.notificationService = notificationService;
    this.kafkaTemplate = kafkaTemplate;
    eventBus.register(this); // eventbus에 이벤트 리스너로 등록한다. 이벤트가 발생하여 이벤트 버스로 쏴지면  @subscribe가 붙어있는 메소드가 실행된다.
    // 어떤 이벤트 리스너인지는 등록 안해주나? 이벤트가 발생하면 리스너의 모든 메소드를 call해주는건가? 아니면 브로드 캐스팅?
  }

  @Subscribe /**https://www.slideshare.net/koneru9999/guavas-event-bus**/
  public void handleJoinEvent(JoinEvent event) throws JsonProcessingException { //  JoinEvent가 버스로 쏴지면 수행됨 => 어떻게 이벤트가 쏴짐을 알아차리지?
    // 리스너를 이벤트 버스에 register 할때, @Subscribe가 붙은 리스너 메소드들을 메소드 파라미터 타입 기반으로 등록한다.
    // 이벤트 버스는 포스팅되는 이벤트의 클래스 타입을 메소드 파라메터로 들고 있는 handler 메소드들을 콜한다. 특이한 점은 List<T> List<S> 제넥릭 타입이 달라도 List이기 때문에 버스는 동일한것으로 간주하고 두 메소드 모두 호출함
    // todo test. JoinEvent 대신 CommentEvent로 바꾸면 로그가 안찍혀야한다.
    String name = event.getName();
    Id<User, Long> userId = event.getUserId();
    log.info("user {}, userId {} joined!", name, userId);

    /**
     * 이벤트 이름(name)을 아래처럼 바로 날리면 안되고 카프카 메시지로 변환해서 보내줘야한다. (엔티티랑 dto 분리한것처럼)  이벤트랑 메시지를 분리해서 joinMessage 같은 객체 만들어 보내야 한다.
     * this.kafkaTemplate.send("test", name); (x)
     * this.kafkaTemplate.send("test", JoinMessage.of(event)); (O)// 특정 이벤트에 대한 카프카 message 생성
     * 어떤 메시지포멧으로 serialize할 건지는 카프카 serializer 이용해서 해야함. -> 자바 obj를 특정 메시지 포멧(json..)로 변환가능. -> application.yml에서 설정
     * this.kafkaTemplate.send("test", ObjectMapper.xx); (x) ObjectMapper로 json 직접 변환하면 절대 안됨.
     */

    // sendMessage // producer topic에다 message send -> kafka  => 컨슈머는 어딧지?
//    this.kafkaTemplate.send("test", "Please send welcome message for " + userId.value()); // todo 메시지도 객체로(dto)로 만들어서 보내야한다.
    this.kafkaTemplate.send(userJoinedTopic, new UserJoinedMessage(event));


    /*
    try {
      log.info("Try to send push for {}", event);
      notificationService.notifyAll(new PushMessage(
        name + " Joined!",
        "/friends/" + userId.value(),
        "Please send welcome message"
      ));
    } catch (Exception e) {
      log.error("Got error while handling event JoinEvent " + event.toString(), e);
      e.printStackTrace();
    }
    */
  }

  //id="group-id1", topics="test"
  @KafkaListener(id = "api", topics = "test")
  public void helloEventListen(String helloEvent) {
    log.info("Got Message from Kafka {}", helloEvent);
  }

  @Override
  public void close() throws Exception {
    eventBus.unregister(this);
  }

}

