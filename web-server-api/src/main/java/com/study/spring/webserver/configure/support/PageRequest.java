package com.study.spring.webserver.configure.support;

import static com.google.common.base.Preconditions.checkArgument;

public class PageRequest implements Pageable {

  private final long offset;

  private final int limit;

  public PageRequest() {
    this(0, 5);
  }

  public PageRequest(long offset, int limit) {
    checkArgument(offset >= 0,"Offset must be greater or equals to zero.");
    checkArgument(limit >= 1,"limit must be greater than zero.");

    this.offset = offset;
    this.limit = limit;
  }

  @Override
  public long offset() {
    return offset;
  }

  @Override
  public int limit() {
    return limit;
  }

  @Override
  public String toString() {
    return "PageRequest{" +
      "offset=" + offset +
      ", limit=" + limit +
      '}';
  }
}