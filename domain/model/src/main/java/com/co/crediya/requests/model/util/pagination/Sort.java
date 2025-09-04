package com.co.crediya.requests.model.util.pagination;

import java.util.ArrayList;
import java.util.List;

public class Sort {
  private final List<Order> orders;

  public Sort(List<Order> orders) {
    this.orders = orders != null ? orders : new ArrayList<>();
  }

  public static Sort by(String... properties) {
    List<Order> orders = new ArrayList<>();
    for (String property : properties) {
      orders.add(Order.asc(property));
    }
    return new Sort(orders);
  }

  public static Sort by(Direction direction, String... properties) {
    List<Order> orders = new ArrayList<>();
    for (String property : properties) {
      orders.add(new Order(direction, property));
    }
    return new Sort(orders);
  }

  public Sort ascending() {
    return new Sort(this.orders.stream().map(order -> Order.asc(order.getProperty())).toList());
  }

  public Sort descending() {
    return new Sort(this.orders.stream().map(order -> Order.desc(order.getProperty())).toList());
  }

  public boolean isSorted() {
    return !orders.isEmpty();
  }

  public boolean isUnsorted() {
    return orders.isEmpty();
  }

  public List<Order> getOrders() {
    return new ArrayList<>(orders);
  }

  // Clase interna Order
  public static class Order {
    private final String property;
    private final Direction direction;

    public Order(Direction direction, String property) {
      this.direction = direction != null ? direction : Direction.ASC;
      this.property = property;
    }

    public static Order asc(String property) {
      return new Order(Direction.ASC, property);
    }

    public static Order desc(String property) {
      return new Order(Direction.DESC, property);
    }

    public Direction getDirection() {
      return direction;
    }

    public String getProperty() {
      return property;
    }

    public boolean isAscending() {
      return Direction.ASC.equals(direction);
    }

    public boolean isDescending() {
      return Direction.DESC.equals(direction);
    }
  }

  public enum Direction {
    DESC,
    ASC;

    public static Direction get(String value) {
      if (value != null && value.trim().equalsIgnoreCase("DESC")) {
        return DESC;
      }
      return ASC;
    }
  }
}
