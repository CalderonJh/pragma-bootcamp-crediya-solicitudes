package com.co.crediya.requests.model.util.pagination;

import java.util.ArrayList;
import java.util.Objects;

public class Pageable {
  private final int pageNumber;
  private final int pageSize;
  private final Sort sort;

  public Pageable(int pageNumber, int pageSize) {
    this(pageNumber, pageSize, new Sort(new ArrayList<>()));
  }

  public Pageable(int pageNumber, int pageSize, Sort sort) {
    this.pageNumber = Math.max(0, pageNumber);
    this.pageSize = Math.max(10, pageSize);
    this.sort = sort != null ? sort : new Sort(new ArrayList<>());
  }

  public static Pageable of(int page, int size) {
    return new Pageable(page, size);
  }

  public static Pageable of(int page, int size, Sort sort) {
    return new Pageable(page, size, sort);
  }

  public static Pageable of(int page, int size, Sort.Direction direction, String... properties) {
    return new Pageable(page, size, Sort.by(direction, properties));
  }

  public int getPageNumber() {
    return pageNumber;
  }

  public int getPageSize() {
    return pageSize;
  }

  public long getOffset() {
    return (long) pageNumber * pageSize;
  }

  public Sort getSort() {
    return sort;
  }

  public Pageable next() {
    return new Pageable(pageNumber + 1, pageSize, sort);
  }

  public Pageable previous() {
    return hasPrevious() ? new Pageable(pageNumber - 1, pageSize, sort) : this;
  }

  public Pageable first() {
    return new Pageable(0, pageSize, sort);
  }

  public boolean hasPrevious() {
    return pageNumber > 0;
  }

  public boolean isPaged() {
    return true;
  }

  public boolean isUnpaged() {
    return false;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null || getClass() != obj.getClass()) return false;

    Pageable pageable = (Pageable) obj;
    return pageNumber == pageable.pageNumber
        && pageSize == pageable.pageSize
        && Objects.equals(sort, pageable.sort);
  }

  @Override
  public int hashCode() {
    return Objects.hash(pageNumber, pageSize, sort);
  }

  @Override
  public String toString() {
    return String.format(
        "Page request [number: %d, size: %d, sort: %s]", pageNumber, pageSize, sort);
  }
}
