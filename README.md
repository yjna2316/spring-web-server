# spring-web-server
프로그래머스 웹백엔드 구현 스터디에서 했었던 내용을 정리한 레퍼지토리 입니다.
[스터디 후기](https://yjna2316.github.io/study/2020/11/05/pg-study-0w/)

## branch 
### 1-Getting_Started_API
- Maven 기반 Spring Boot 프로젝트 처음부터 직접 생성
- 간단한 User API 생성
    - 유저 정보 저장 / 조회링
- Controller 테스트 코
- JDBCTemplate, h2 이용 
- 상세 내용 : [스터디 1주차 미션 후기](https://yjna2316.github.io/study/2020/11/12/pg-study-1w/)

### 2-Social_Server
- Spring Security 커스터마이징
    - JWT(Json Web Token)로 Sessionless한 인증 처리 
    - 본인과 친구관계에만 포스트 공개 범위 제한하는 Voter 구현  
        - 전략 패턴으로 확장에 유연하도록 리팩토링  (IoC 원칙 적용)
- 헬스체크 & 이메일 중복 확인 API 추가
- JDBCTemplate, h2 이용 
- 상세 내용 : [스터디 2주차 미션 후기](https://yjna2316.github.io/study/2020/11/19/pg-study-2w/)
