package net.luency.donut.service.order.queue;

import com.google.common.collect.Lists;
import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Optional;
import java.util.Collection;

import org.springframework.stereotype.Repository;
import org.springframework.beans.factory.annotation.Autowired;

import net.luency.donut.service.order.Order;

@Repository
public class InMemoryOrderQueue implements OrderQueue {
  private final List<Order> pendingOrders = Lists.newCopyOnWriteArrayList();

  private final OrderPriorityComparator comparator;

  @Autowired
  private InMemoryOrderQueue(OrderPriorityComparator comparator) {
    this.comparator = comparator;
  }

  @Override
  public List<Order> list() {
    return ImmutableList.copyOf(pendingOrders);
  }

  @Override
  public Order add(Order order) {
    pendingOrders.add(order);
    pendingOrders.sort(comparator);
    return order;
  }

  @Override
  public boolean exists(int customerId) {
    return pendingOrders.stream()
      .anyMatch(order -> order.getCustomer().getId() == customerId);
  }

  @Override
  public Optional<Order> get(int customerId) {
    return pendingOrders.stream()
      .filter(order -> order.getCustomer().getId() == customerId)
      .findAny();
  }

  @Override
  public Order remove(Order order) {
    pendingOrders.remove(order);
    sortIfNotEmpty();
    return order;
  }

  @Override
  public void removeAll(Collection<Order> orders) {
    pendingOrders.removeAll(orders);
    sortIfNotEmpty();
  }

  @Override
  public boolean isEmpty() {
    return pendingOrders.isEmpty();
  }

  @Override
  public void clear() {
    pendingOrders.clear();
  }

  private void sortIfNotEmpty() {
    if (pendingOrders.isEmpty()) {
      return;
    }
    pendingOrders.sort(comparator);
  }
}