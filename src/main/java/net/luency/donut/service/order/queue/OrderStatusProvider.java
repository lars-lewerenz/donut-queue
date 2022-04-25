package net.luency.donut.service.order.queue;

import java.time.Instant;
import java.time.temporal.Temporal;
import java.time.temporal.ChronoUnit;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import net.luency.donut.service.cart.Cart;
import net.luency.donut.service.order.Order;
import net.luency.donut.service.order.OrderDto;
import net.luency.donut.service.config.OrderConfig;
import net.luency.donut.service.cart.CartFullException;

@Component
public class OrderStatusProvider {

  private final OrderConfig orderConfig;
  private final OrderQueue orderQueue;

  @Autowired
  public OrderStatusProvider(OrderConfig orderConfig, OrderQueue orderQueue) {
    this.orderConfig = orderConfig;
    this.orderQueue = orderQueue;
  }

  public QueueStatus get(Order order, Temporal lastPickup) {
    int place = calculatePlace(order);
    long approximateWaitingTime = calculateApproximateWaitingTime(order, lastPickup);
    OrderDto orderDto = order.toDto();
    return QueueStatus.create(orderDto, place, approximateWaitingTime);
  }

  private long calculateApproximateWaitingTime(Order order, Temporal lastPickup) {
    int upcomingSchedules = calculateUpcomingDeliveries(order);
    long timePassedSinceLastPickup = ChronoUnit.MILLIS.between(lastPickup, Instant.now());
    long durationToNextSchedule = orderConfig.getOrderScheduleInterval() - timePassedSinceLastPickup;
    long durationForSchedules = orderConfig.getOrderScheduleInterval() * upcomingSchedules;
    return durationToNextSchedule + durationForSchedules;
  }

  private int calculateUpcomingDeliveries(Order order) {
    Cart cart = Cart.create(orderConfig.getMaxDonutsPerCart());
    int upcomingSchedules = 0;
    for (Order toAdd : orderQueue.list()) {
      if (!cart.hasEnoughSpaceForOrder(toAdd)) {
        cart = Cart.create(orderConfig.getMaxDonutsPerCart());
        upcomingSchedules++;
      }
      addOrderToCarOrThrow(cart, toAdd);
      if (cart.contains(order)) {
        break;
      }
    }
    return upcomingSchedules;
  }

  private void addOrderToCarOrThrow(Cart cart, Order toAdd) {
    try {
      cart.addOrder(toAdd);
    } catch (CartFullException cartIsFull) {
      // This order cannot be processed because it must not be split and does
      // not fit in the cart. In this case, maxDonutsPerOrder and maxDonutsPerCart might
      // not have been configured properly.
      throw new IllegalStateException(cartIsFull);
    }
  }

  private int calculatePlace(Order order) {
    return orderQueue.list().indexOf(order) + 1;
  }
}