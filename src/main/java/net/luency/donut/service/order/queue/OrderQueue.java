package net.luency.donut.service.order.queue;

import java.util.List;
import java.util.Optional;
import java.util.Collection;

import net.luency.donut.service.order.Order;

public interface OrderQueue {
  List<Order> list();

  Order add(Order order);

  boolean exists(int customerId);

  Optional<Order> get(int customerId);

  Order remove(Order order);

  void removeAll(Collection<Order> orders);

  boolean isEmpty();

  void clear();
}
