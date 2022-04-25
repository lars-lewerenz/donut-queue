package net.luency.donut.service.order.queue;

import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;

import net.luency.donut.service.order.Order;
import net.luency.donut.service.customer.Customer;

@SpringBootTest
class InMemoryOrderQueueTests {

  @Autowired
  private InMemoryOrderQueue queue;

  @BeforeEach
  void cleanUpQueue() {
    queue.clear();
  }

  @Test
  void givenCustomerIdAndDonutQuantity_whenCreatingOrder_thenShouldContain() {
    Customer customer = Customer.create(1);
    Order order = Order.create(customer, 1);
    queue.add(order);
    assertTrue(queue.exists(customer.getId()));
  }

  @Test
  void givenCustomerIdAndDonutQuantity_whenDeletingOrder_thenShouldNotContain() {
    Customer customer = Customer.create(1);
    Order order = Order.create(customer, 1);
    queue.add(order);
    queue.remove(order);
    assertFalse(queue.exists(customer.getId()));
  }

  @Test
  void givenCustomerIdAndDonutQuantity_whenCreatingOrder_thenShouldFind() {
    Customer customer = Customer.create(1);
    Order expectedOrder = Order.create(customer, 1);
    queue.add(expectedOrder);
    Order actualOrder = queue.get(customer.getId()).get();
    assertEquals(expectedOrder, actualOrder);
  }

  @Test
  void givenOrders_whenListing_thenShouldBePrioritized() {
    Customer normalCustomer = Customer.create(1);
    Order normalOrder = Order.create(normalCustomer, 1);
    Customer premiumCustomer = createPremiumCustomer(2);
    Order prioritizedOrder = Order.create(premiumCustomer, 1);
    queue.add(normalOrder);
    queue.add(prioritizedOrder);
    assertEquals(prioritizedOrder, queue.list().get(0));
  }

  @Test
  void givenManyOrders_whenRemovingOne_thenShouldBePrioritized() {
    Customer normalCustomer = Customer.create(1);
    Order firstOrder = Order.create(normalCustomer, 1);
    Customer firstPremiumCustomer = createPremiumCustomer(2);
    Order secondOrder = Order.create(firstPremiumCustomer, 1);
    Customer secondPremiumCustomer = createPremiumCustomer(3);
    Order thirdOrder = Order.create(secondPremiumCustomer, 1);
    queue.add(firstOrder);
    queue.add(secondOrder);
    queue.add(thirdOrder);
    queue.remove(thirdOrder);
    assertEquals(secondOrder, queue.list().get(0));
  }

  @Test
  void givenOrders_whenRemoving_thenQueueShouldBeEmpty() {
    Customer firstCustomer = Customer.create(1);
    Order firstOrder = Order.create(firstCustomer, 1);
    Customer secondCustomer = createPremiumCustomer(2);
    Order secondOrder = Order.create(secondCustomer, 1);
    queue.add(firstOrder);
    queue.add(secondOrder);
    queue.removeAll(Arrays.asList(firstOrder, secondOrder));
    assertTrue(queue.isEmpty());
  }

  private Customer createPremiumCustomer(int id) {
    Customer customer = Customer.create(id);
    customer.enablePremiumPriority();
    return customer;
  }
}