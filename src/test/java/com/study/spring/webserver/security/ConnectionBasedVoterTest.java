package com.study.spring.webserver.security;

import com.study.spring.webserver.model.commons.Id;
import com.study.spring.webserver.model.user.Email;
import com.study.spring.webserver.model.user.Role;
import com.study.spring.webserver.model.user.User;
import com.study.spring.webserver.service.user.UserService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.util.matcher.RegexRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.lang3.math.NumberUtils.toLong;
import static org.assertj.core.util.Lists.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.security.core.authority.AuthorityUtils.createAuthorityList;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ConnectionBasedVoterTest {

  @Mock
  UserService userService;

  ConnectionBasedVoter voter;

  JwtAuthenticationToken authentication;

  @BeforeAll
  void setUp() {
    userService = mock(UserService.class);
    given(userService.findConnectedIds(any())).willReturn(
      new ArrayList<Id<User, Long>>() {{
        add(Id.of(User.class, 2L));
      }}
    );
    String regex = "^/api/user/(\\d+)/post/.*$";
    voter = newConnectionBasedVoter(regex);
    voter.setUserService(userService);

    authentication = new JwtAuthenticationToken(
      new JwtAuthentication(1L,  new Email("test00@gmail.com"), "tester"),
      null,
      createAuthorityList(Role.USER.value())
    );
  }


  @Test
  void 판단대상_URL이_아닌경우() {
    HttpServletRequest request = mock(HttpServletRequest.class);
    given(request.getServletPath()).willReturn("/api/user/me");
    //given(request.getRequestURI()).willReturn("/api/user/me");

    FilterInvocation fi = mock(FilterInvocation.class);
    given(fi.getRequest()).willReturn(request);

    int result = voter.vote(authentication, fi, emptyList());
    assertThat(result, is(AccessDecisionVoter.ACCESS_GRANTED));
  }

  @Test
  void 본인자신이라면_접근가능() {
    HttpServletRequest request = mock(HttpServletRequest.class);
    given(request.getServletPath()).willReturn("/api/user/1/post/list");
    given(request.getRequestURI()).willReturn("/api/user/1/post/list");

    FilterInvocation fi = mock(FilterInvocation.class);
    given(fi.getRequest()).willReturn(request);

    int result = voter.vote(authentication, fi, emptyList());
    assertThat(result, is(AccessDecisionVoter.ACCESS_GRANTED));
  }

  @Test
  void 친구관계_접근가능() {
    HttpServletRequest request = mock(HttpServletRequest.class);
    given(request.getServletPath()).willReturn("/api/user/2/post/list");
    given(request.getRequestURI()).willReturn("/api/user/2/post/list");

    FilterInvocation fi = mock(FilterInvocation.class);
    given(fi.getRequest()).willReturn(request);

    int result = voter.vote(authentication, fi, emptyList());
    assertThat(result, is(AccessDecisionVoter.ACCESS_GRANTED));
  }

  @Test
  void 친구관계가_아니면_접근불가() {
    HttpServletRequest request = mock(HttpServletRequest.class);
    given(request.getServletPath()).willReturn("/api/user/1000/post/list");
    given(request.getRequestURI()).willReturn("/api/user/1000/post/list");

    FilterInvocation fi = mock(FilterInvocation.class);
    given(fi.getRequest()).willReturn(request);

    int result = voter.vote(authentication, fi, emptyList());
    assertThat(result, is(AccessDecisionVoter.ACCESS_DENIED));
  }

  private ConnectionBasedVoter newConnectionBasedVoter(String regex) {
    final Pattern pattern = Pattern.compile(regex);
    RequestMatcher requiresAuthorizationRequestMatcher = new RegexRequestMatcher(pattern.pattern(), null);
    return new ConnectionBasedVoter(
      requiresAuthorizationRequestMatcher,
      (String url) -> {
        /* url에서 targetId를 추출하기 위해 정규식 처리 */
        Matcher matcher = pattern.matcher(url);
        long id = matcher.matches() ? toLong(matcher.group(1), -1) : -1;
        return Id.of(User.class, id);
      }
    );
  }

}
