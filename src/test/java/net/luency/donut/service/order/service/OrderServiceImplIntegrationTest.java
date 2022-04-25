package net.luency.donut.service.order.service;

import java.util.Optional;
import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;

import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.junit.jupiter.api.Assertions.assertEquals;

import net.luency.donut.service.order.Order;
import net.luency.donut.service.customer.Customer;
import net.luency.donut.service.order.queue.OrderQueue;

@SpringBootTest
class OrderServiceImplIntegrationTest {
  @MockBean
  private OrderQueue orderQueue;
  @Autowired
  private OrderService orderService;

  @BeforeEach
  void mockOrderService() {
    Order expectedOrder = createOrder();
    when(orderQueue.add(any())).thenReturn(expectedOrder);
    when(orderQueue.get(anyInt())).thenReturn(Optional.of(expectedOrder));
    when(orderQueue.list()).thenReturn(Collections.singletonList(expectedOrder));
    when(orderQueue.exists(anyInt())).thenReturn(true);
  }

  private Order createOrder() {
    return Order.create(Customer.create(1), 1);
  }

  @Test
  void givenCustomerIdAndDonutQuantity_whenCreatingOrder_thenShouldEquals() {
    Order expectedOrder = createOrder();
    when(orderQueue.exists(1)).thenReturn(false);
    Order actualOrder = orderService.create(1, 1);
    assertEquals(expectedOrder.getCustomer().getId(), actualOrder.getCustomer().getId());
    assertEquals(expectedOrder.getStatus(), actualOrder.getStatus());
    assertEquals(expectedOrder.getDonutQuantity(), actualOrder.getDonutQuantity());
  }

  @Test
  void givenOrder_whenCancelingOrder_thenShouldBeCanceled() {
    Order actualOrder = orderService.cancel(1).get();
    assertEquals(Order.Status.CANCELLED, actualOrder.getStatus());
  }

  @Test
  void givenCustomerId_whenRetrievingOrder_thenShouldEquals() {
    Order expectedOrder = createOrder();
    Order actualOrder = orderService.get(1).get();
    assertEquals(expectedOrder.getCustomer().getId(), actualOrder.getCustomer().getId());
    assertEquals(expectedOrder.getStatus(), actualOrder.getStatus());
    assertEquals(expectedOrder.getDonutQuantity(), actualOrder.getDonutQuantity());
  }
}