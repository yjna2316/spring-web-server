package com.study.spring.webserver.controller;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import org.springframework.http.HttpStatus;

/* API Response 공통 FORMAT */
public class ApiResult<T> {

  private final boolean isSuccess;

  @JsonInclude(Include.NON_NULL)
  private final  T response;

  @JsonInclude(Include.NON_NULL)
  private final ApiError errors;

  private ApiResult(boolean isSuccess, T response, ApiError error) {
    this.isSuccess = isSuccess;
    this.response = response;
    this.errors = error;
  }

  public static <T> ApiResult<T> OK(T response) {
    return new ApiResult<>(true, response, null);
  }

  public static ApiResult<?> ERROR(ErrorCode errorCode, HttpStatus status) {
    return new ApiResult<>(false, null, new ApiError(errorCode, status) );
  }

  public boolean isSuccess() {
    return isSuccess;
  }

  public T getResponse() {
    return response;
  }

  public ApiError getErrors() {
    return errors;
  }
}
