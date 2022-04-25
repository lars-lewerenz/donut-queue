package net.luency.donut.service.order;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import net.luency.donut.service.customer.Customer;

class OrderTests {

  @Test
  void givenOrder_whenDelivered_thenStatusShouldBeDelivered() {
    Order order = Order.create(Customer.create(1), 1);
    order.deliver();
    assertEquals(Order.Status.DELIVERED, order.getStatus());
  }

  @Test
  void givenOrder_whenCanceled_thenStatusShouldBeCanceled() {
    Order order = Order.create(Customer.create(1), 1);
    order.cancel();
    assertEquals(Order.Status.CANCELLED, order.getStatus());
  }
}