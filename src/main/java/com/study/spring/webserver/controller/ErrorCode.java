package com.study.spring.webserver.controller;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

  USER_NOT_FOUND("M001", "유저를 찾을 수 없습니다."),
  EMAIL_DUPLICATED("M002", "중복된 이메일입니다."),
  INTERNAL_SERVER_ERROR("E001", "서버 내부 에러 입니다.")
  ;

  private final String code;
  private final String message;

  // ? e.getMessage는 어떻게 표시하지? => error log로 남긴다. 트래킹을 위함
  // Enum 내부에서 생성자를 어떻게 호출 하는 거지?

  ErrorCode(final String code, final String message) {
    this.code = code;
    this.message = message;
  }

  public String getCode() {
    return code;
  }

  public String getMessage() {
    return message;
  }
}
