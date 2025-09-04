package com.co.crediya.requests.model.util.pagination;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class Page<T> {
  private final List<T> content;
  private final Pageable pageable;
  private final long totalElements;

  public Page(List<T> content, Pageable pageable, long totalElements) {
    this.content = content != null ? new ArrayList<>(content) : new ArrayList<>();
    this.pageable = pageable;
    this.totalElements = totalElements;
  }

  public Page(List<T> content) {
    this(
        content,
        com.co.crediya.requests.model.util.pagination.Pageable.of(
            0, content != null ? content.size() : 0),
        content != null ? content.size() : 0);
  }

  // Contenido
  public List<T> getContent() {
    return new ArrayList<>(content);
  }

  public boolean hasContent() {
    return !content.isEmpty();
  }

  public int getNumberOfElements() {
    return content.size();
  }

  // Información de paginación
  public int getNumber() {
    return pageable.getPageNumber();
  }

  public int getSize() {
    return pageable.getPageSize();
  }

  public Sort getSort() {
    return pageable.getSort();
  }

  public Pageable getPageable() {
    return pageable;
  }

  // Totales
  public long getTotalElements() {
    return totalElements;
  }

  public int getTotalPages() {
    return getSize() == 0 ? 1 : (int) Math.ceil((double) totalElements / getSize());
  }

  // Navegación
  public boolean hasNext() {
    return getNumber() + 1 < getTotalPages();
  }

  public boolean hasPrevious() {
    return getNumber() > 0;
  }

  public boolean isFirst() {
    return !hasPrevious();
  }

  public boolean isLast() {
    return !hasNext();
  }

  public Pageable nextPageable() {
    return hasNext() ? pageable.next() : pageable;
  }

  public Pageable previousPageable() {
    return hasPrevious() ? pageable.previous() : pageable;
  }

  // Iteración
  public boolean isEmpty() {
    return content.isEmpty();
  }

  // Métodos de transformación
  public <U> Page<U> map(Function<T, U> converter) {
    List<U> convertedContent = content.stream().map(converter).toList();
    return new Page<>(convertedContent, pageable, totalElements);
  }

  @Override
  public String toString() {
    String contentType = "UNKNOWN";
    if (!content.isEmpty()) {
      contentType = content.getFirst().getClass().getName();
    }

    return String.format(
        "Page %d of %d containing %s instances", getNumber() + 1, getTotalPages(), contentType);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null || getClass() != obj.getClass()) return false;

    Page<?> page = (Page<?>) obj;
    return Objects.equals(content, page.content)
        && Objects.equals(pageable, page.pageable)
        && totalElements == page.totalElements;
  }

  @Override
  public int hashCode() {
    return Objects.hash(content, pageable, totalElements);
  }
}
