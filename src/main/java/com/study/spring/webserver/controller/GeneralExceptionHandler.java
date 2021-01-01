package com.study.spring.webserver.controller;

import com.study.spring.webserver.error.DuplicateEmailException;
import com.study.spring.webserver.error.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static com.study.spring.webserver.controller.ApiResult.ERROR;

@RestControllerAdvice("controller")
@ControllerAdvice
public class GeneralExceptionHandler {

  private final Logger log = LoggerFactory.getLogger(getClass());

  private ResponseEntity<ApiResult<?>> newResponse(ErrorCode errorCode, HttpStatus status) {
    HttpHeaders headers = new HttpHeaders();
    headers.add("Content-Type", "application/json");
    return new ResponseEntity<>(ERROR(errorCode, status), headers, status);
  }

  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<?> handleNotFoundException(NotFoundException e) {
    log.warn("Not found exception occurred: {}", e.getMessage(), e);
    return newResponse(ErrorCode.USER_NOT_FOUND, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(DuplicateEmailException.class)
  public ResponseEntity<?> handleDuplicateEmailException(DuplicateEmailException e) {
    log.warn("Duplicate Email exception occurred: {}", e.getMessage(), e);
    return newResponse(ErrorCode.EMAIL_DUPLICATED, HttpStatus.CONFLICT);
  }

  @ExceptionHandler({Exception.class, RuntimeException.class})
  public ResponseEntity<?> handleException(Exception e) {
    log.error("Unexpected exception occurred: {}", e.getMessage(), e);
    return newResponse(ErrorCode.INTERNAL_SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
