# spring-web-server
프로그래머스 웹백엔드 구현 스터디에서 했었던 내용을 정리한 레퍼지토리 입니다.

## branch 
### 1-Getting_Started_API
- Maven 기반 Spring Boot 프로젝트 처음부터 직접 생성
- 간단한 User API 생성 (Spring MVC)
- JdbcTemplate를 이용한 H2 Database 저장
- Controller 테스트 코드 작성  
    - [스터디 1주차 미션 후기](https://yjna2316.github.io/study/2020/11/12/pg-study-1w/)

### 2-Social_Server
- Spring Security를 활용한 JWT 기반의 유저 인증 및 권한 설정
    - 본인과 친구관계에만 포스트 공개 범위 제한 Voter  
    - 전략 패턴으로 확장에 유연하도록 리팩토링 (IoC 원칙 적용)
- Post API 
    - ArgumentResolver 이용한 Paging 처리
    - 포스트 좋아요와 댓글 구현
- Java8 CompletableFuture를 이용한 AWS S3 이미지 업로드 비동기 처리 구현
- Swagger
- JdbcTemplate를 이용한 H2 Database 저장
- Service 테스트 코드 작성
     - [스터디 2주차 미션 후기](https://yjna2316.github.io/study/2020/11/19/pg-study-2w/)
     - [스터디 3주차 미션 후기](https://yjna2316.github.io/study/2020/11/26/pg-study-3w/)
     - [스터디 4주차 미션 후기](https://yjna2316.github.io/study/2020/12/03/pg-study-4w/)
    
### 3-Social_Server_Kafka_Push

- 멀티모듈화 부모-자식 프로젝트로 변경
    - web-server-api
    - web-server-push
- MSA와 Event Driven 방식에 대한 이해
- Message Queue(카프카)를 이용한 비동기 웹 푸시 알림 기능 구현    
    - [스터디 5주차 미션 후기](https://yjna2316.github.io/study/2020/12/10/pg-study-5w/)
     