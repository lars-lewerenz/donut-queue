package net.luency.donut.service.order.service;

import java.util.Optional;

import net.luency.donut.service.cart.Cart;
import net.luency.donut.service.order.Order;
import net.luency.donut.service.order.queue.QueueSnapshot;
import net.luency.donut.service.order.queue.QueueStatus;

public interface OrderService {

  boolean exists(int customerId);

  Optional<Order> get(int customerId);

  Order create(int customerId, int donutQuantity);

  Optional<Order> cancel(int customerId);

  Optional<QueueStatus> getQueueStatus(int customerId);

  Optional<Cart> retrieveNextDelivery();

  QueueSnapshot getQueueSnapshot();
}
