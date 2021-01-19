package com.study.spring.webserver.configure.support;

import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import static java.lang.Integer.min;
import static org.apache.commons.lang3.math.NumberUtils.toInt;
import static org.springframework.util.StringUtils.hasText;

public class PageableArgumentResolver implements HandlerMethodArgumentResolver {
  private static final String DEFAULT_OFFSET_PARAMETER = "offset";

  private static final String DEFAULT_LIMIT_PARAMETER = "limit";

  private static final int DEFAULT_MAX_LIMIT_SIZE = 5;

  private PageRequest fallbackPageable; // 만일의 사태에 대비한 pageable

  private String offsetParameterName = DEFAULT_OFFSET_PARAMETER;

  private String limitParameterName = DEFAULT_LIMIT_PARAMETER;

  /**
   * supportsParameter()
   * - 원하는 타입의 인자가 있는지 검사한 후 있을 경우 true가 리턴되도록 함
   * - Controller에 요청된 타입이 Pageable 인터페이스 구현체인지 체크
   *
   * isAssignableFrom()
   * - 다형성 체크 메소드 / 치환 가능 여부 => If same as or superclass or superinterface of given then return true
   * - isAssignableFrom() 대신  isEquals()을 사용하게 될 경우, Controller 파라미터 타입으로 인터페이스(Pageable)대신 다른 구현체 사용시(simplePage..) false 반환 가능(PR 참조)
   */
  @Override
  public boolean supportsParameter(MethodParameter parameter) {
    // MethodParameter parameter =>  PostRestController의 PageRequest pageable 파라메터 의미
    return Pageable.class.isAssignableFrom(parameter.getParameterType());
  }

  /**
   * resolveArgument()
   * 메소드 인자로 전달할 값을 리턴
   */
  @Override
  public Object resolveArgument(
    MethodParameter parameter,
    ModelAndViewContainer mavContainer,
    NativeWebRequest webRequest,
    WebDataBinderFactory binderFactory
  ) throws Exception {
    String offsetString = webRequest.getParameter(offsetParameterName);
    String limitString = webRequest.getParameter(limitParameterName);

    // null이거나 음수이면 기본값으로(offset=0, limit=5) 세팅
    long offset = hasText(offsetString)? parseAndCheckBoundaries(offsetString, Integer.MAX_VALUE) : fallbackPageable.offset();
    int limit = hasText(limitString)? parseAndCheckBoundaries(limitString, DEFAULT_MAX_LIMIT_SIZE) : fallbackPageable.limit();
    limit = limit < 1 ? fallbackPageable.limit() : limit;

    return new PageRequest(offset, limit);
  }

  // 양수면 min(인자, 상한선), 음수면 0.
  public int parseAndCheckBoundaries(String value, int upper) {
    int parsed = toInt(value);
    return parsed < 0 ? 0: min(parsed, upper);
  }

  public void setFallbackPageable(PageRequest fallbackPageable) {
    this.fallbackPageable = fallbackPageable;
  }
}

