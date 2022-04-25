package net.luency.donut.service.cart;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import net.luency.donut.service.order.Order;
import net.luency.donut.service.customer.Customer;

class CartTests {

  @Test
  void givenCapacityAndTooManyOrders_whenFillingCart_thenFail() {
    Cart cart = Cart.create(10);
    assertThrows(CartFullException.class, () -> {
      cart.addOrder(Order.create(Customer.create(1), 5));
      cart.addOrder(Order.create(Customer.create(2), 6));
    });
  }

  @Test
  void givenCapacityAndOrders_whenFillingCart_thenDoNotFail() {
    Cart cart = Cart.create(10);
    assertThrows(CartFullException.class, () -> {
      cart.addOrder(Order.create(Customer.create(1), 5));
      cart.addOrder(Order.create(Customer.create(2), 6));
    });
  }

  @Test
  void givenCapacityAndOrders_whenFillingCart_thenCheckQuantity() throws CartFullException {
    Cart cart = Cart.create(10);
    cart.addOrder(Order.create(Customer.create(1), 1));
    cart.addOrder(Order.create(Customer.create(2), 2));
    int actualQuantity = cart.getDonutQuantity();
    int expectedQuantity = 3;
    assertEquals(expectedQuantity, actualQuantity);
  }

  @Test
  void givenOrder_whenAddingToCart_thenShouldContain() throws CartFullException {
    Cart cart = Cart.create(10);
    Order order = Order.create(Customer.create(1), 1);
    cart.addOrder(order);
    assertTrue(cart.contains(order));
  }

  @Test
  void givenCart_whenMappingToDto_thenShouldBeEqual() throws CartFullException {
    Cart cart = Cart.create(10);
    Order order = Order.create(Customer.create(1), 1);
    cart.addOrder(order);
    CartDto actualCartDto = cart.toDto();
    assertEquals(cart.getCapacity(), actualCartDto.getCapacity());
    assertEquals(cart.getOrders().size(), actualCartDto.getOrders().size());
  }
}