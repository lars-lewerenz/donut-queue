package net.luency.donut.service.order.queue;

import org.springframework.stereotype.Component;

import java.util.Comparator;

import net.luency.donut.service.order.Order;

@Component
public final class OrderPriorityComparator implements Comparator<Order> {
  private OrderPriorityComparator() {}

  @Override
  public int compare(Order first, Order second) {
    // descending order, more prioritized order should be the first in the list
    return second.compareTo(first);
  }
}
