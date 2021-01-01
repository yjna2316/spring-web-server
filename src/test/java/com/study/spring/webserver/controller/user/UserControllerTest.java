package com.study.spring.webserver.controller.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.spring.webserver.controller.ApiResult;
import com.study.spring.webserver.controller.ErrorCode;
import com.study.spring.webserver.error.DuplicateEmailException;
import com.study.spring.webserver.error.NotFoundException;
import com.study.spring.webserver.model.user.Email;
import com.study.spring.webserver.model.user.User;
import com.study.spring.webserver.repository.user.UserRepository;
import com.study.spring.webserver.service.user.UserService;
import org.junit.jupiter.api.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.study.spring.webserver.controller.ApiResult.ERROR;
import static com.study.spring.webserver.controller.ApiResult.OK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@RunWith(SpringRunner.class)
// @WebMvcTest -> MVC 위한 테스트. 웹에서 테스트 하기 힘든 컨트롤러를 테스트하는데 적합
// MVC 관련 설정인 @Controller, @ControllerAdvice, @JsonComponent, Filter, WebMvcConfigurer, HandlerMethodArgumentResolver만 로드된다
@WebMvcTest(controllers = UserController.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserControllerTest {

  @Autowired // 주입된 MockMvc는 컨트롤러 테스트시 모든 의존성을 로드 아닌 해당 컨트롤러 관련 빈만 로드하여 가벼운 테스트 수행 가능
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean // 가짜 객체. 특정 행위 지정해서 실제 객체처럼 동작하게 만들 수 있다.
  private UserService userMockService;

  @MockBean
  private UserRepository userRepository;

  private Email email;

  private String password;

  @BeforeAll
  void setUp() {
    email = new Email("test@gmail.com");
    password = "1234";
  }


  @Test
  @Order(1)
  public void 유저_가입이_성공한다() throws Exception {
    JoinRequest requestDto = new JoinRequest(email.getAddress(), password);
    User user = new User(email, password);
    ApiResult apiResult = OK(new UserDto(user));

    given(userMockService.join(any(), any())).willReturn(user);

    mockMvc.perform(post("/api/user/join")
      .contentType(MediaType.APPLICATION_JSON)
      .content(objectMapper.writeValueAsString(requestDto)))
      .andExpect(status().isOk())
      .andExpect(content().contentType(MediaType.APPLICATION_JSON))
      .andExpect(jsonPath("$.success", is(true)))
      .andExpect(content().string(objectMapper.writeValueAsString(apiResult)))
      .andDo(print());
  }

  @Test
  @Order(2)
  public void 이미_존재하는_이메일로_가입하는_경우_실패한다() throws Exception {
    JoinRequest requestDto = new JoinRequest(email.getAddress(), password);

    given(userMockService.join(any(), any())).willThrow(DuplicateEmailException.class);
    ApiResult<?> expectedError = ERROR(ErrorCode.EMAIL_DUPLICATED, HttpStatus.CONFLICT);

    // when
    MockHttpServletResponse response = mockMvc.perform(post("/api/user/join")
      .contentType(MediaType.APPLICATION_JSON)
      .content(objectMapper.writeValueAsString(requestDto)))
      .andDo(print())
      .andReturn()
      .getResponse();

//    System.out.println("response body: " + response.getContentAsString());
    // then
    assertThat(response.getContentType().equals(MediaType.APPLICATION_JSON));
    assertThat(response.getContentAsString(Charset.defaultCharset())).isEqualTo(objectMapper.writeValueAsString(expectedError));

  }


  @Test
  @Order(3)
  public void 전체_유저를_조회한다() throws Exception {
    // given
    User user1 = new User(new Email("test1@gmail.com"), "test111");
    User user2 = new User(new Email("test2@gmail.com"), "test222");

    List<User> userList = new ArrayList<>();
    userList.add(user1);
    userList.add(user2);

    given(userMockService.findAll()).willReturn(userList);

    // when
    ResultActions actions = mockMvc.perform(get("/api/user/list")
      .contentType(MediaType.APPLICATION_JSON))
      .andDo(print());

    // then
    actions
      .andExpect(status().isOk())
      .andExpect(content().contentType(MediaType.APPLICATION_JSON))
      .andExpect(jsonPath("$.length()", is(2)))
      .andExpect(jsonPath("$.response[0].email.address", is(user1.getEmail().getAddress())))
      .andExpect(jsonPath("$.response[0].password", is(user1.getPassword())))
      .andExpect(jsonPath("$.response[1].email.address", is(user2.getEmail().getAddress())))
      .andExpect(jsonPath("$.response[1].password", is(user2.getPassword())))
      .andDo(print());
  }

  @Test
  @Order(4)
  public void 유저_아이디로_한명을_조회한다() throws Exception {
    // given
    User user = new User(email, password);
    ApiResult apiResult = OK(new UserDto(user));

    given(userMockService.findById(1L)).willReturn(Optional.of(user));

    // when
    MockHttpServletResponse response = mockMvc.perform(get("/api/user/1")
      .accept(MediaType.APPLICATION_JSON))
      .andDo(print())
      .andReturn().getResponse();

    // then
    assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
    assertThat(response.getContentAsString()).isEqualTo(objectMapper.writeValueAsString(apiResult));
  }

  @Test
  @Order(5)
  public void 없는_유저를_조회하면_에러() throws Exception {
    // given
    given(userMockService.findById(2L)).willThrow(NotFoundException.class);
    ApiResult<?> expectedError = ERROR(ErrorCode.USER_NOT_FOUND, HttpStatus.NOT_FOUND);

    // when
    MockHttpServletResponse response = mockMvc.perform(get("/api/user/2")
      .contentType(MediaType.APPLICATION_JSON))
      .andDo(print())
      .andReturn()
      .getResponse();

    // then
    assertThat(response.getContentType().equals(MediaType.APPLICATION_JSON));
    assertThat(response.getContentAsString(Charset.defaultCharset())).isEqualTo(objectMapper.writeValueAsString(expectedError));
  }
}
