package net.luency.donut.service.order.queue;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;

import net.luency.donut.service.order.Order;
import net.luency.donut.service.customer.Customer;

@SpringBootTest
class OrderPriorityComparatorTests {

  @Autowired
  private OrderPriorityComparator comparator;

  @Test
  void givenTwoOrders_whenCompared_thenFirstShouldBePrioritized() {
    Order firstOrder = Order.create(Customer.create(1), 1);
    Customer secondCustomer = Customer.create(2);
    secondCustomer.enablePremiumPriority();
    Order secondOrder = Order.create(secondCustomer, 1);
    assertEquals(1, comparator.compare(firstOrder, secondOrder));
  }

  @Test
  void givenTwoSamePrioritizedOrders_whenCompared_thenEarlierCreatedShouldBePrioritized() {
    Order firstOrder = Order.create(Customer.create(1), 1);
    try {
      // this allows the second order to have an advanced timestamp
      Thread.sleep(1);
    } catch (InterruptedException interruption) {
      interruption.printStackTrace();
      Thread.currentThread().interrupt();
      return;
    }
    Order secondOrder = Order.create(Customer.create(2), 1);
    assertTrue(comparator.compare(secondOrder, firstOrder) > 0);
  }
}