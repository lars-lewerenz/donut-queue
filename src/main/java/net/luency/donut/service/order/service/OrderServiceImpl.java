package net.luency.donut.service.order.service;

import com.google.common.base.Preconditions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.time.Instant;
import java.util.stream.Collectors;
import java.time.temporal.Temporal;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import net.luency.donut.service.cart.Cart;
import net.luency.donut.service.order.Order;
import net.luency.donut.service.config.OrderConfig;
import net.luency.donut.service.customer.Customer;
import net.luency.donut.service.order.queue.OrderQueue;
import net.luency.donut.service.cart.CartFullException;
import net.luency.donut.service.order.queue.QueueStatus;
import net.luency.donut.service.order.queue.QueueSnapshot;
import net.luency.donut.service.order.queue.OrderStatusProvider;

@Service
public class OrderServiceImpl implements OrderService {
  private static final Logger LOG = LoggerFactory.getLogger(OrderServiceImpl.class);

  private final OrderQueue orderQueue;
  private final OrderConfig orderConfig;
  private final OrderStatusProvider statusProvider;

  private Temporal lastPickup = Instant.now();

  @Autowired
  public OrderServiceImpl(
    OrderQueue orderQueue,
    OrderConfig orderConfig,
    OrderStatusProvider statusProvider
  ) {
    this.orderQueue = orderQueue;
    this.orderConfig = orderConfig;
    this.statusProvider = statusProvider;
  }

  @Override
  public Order create(int customerId, int donutQuantity) {
    Preconditions.checkArgument(
      donutQuantity <= orderConfig.getMaxDonutsPerOrder(),
      "Too many donuts for one order. Max donuts: " + orderConfig.getMaxDonutsPerCart()
    );
    if (exists(customerId)) {
      throw new IllegalArgumentException("Only one order can be placed per client at a time.");
    }
    Customer customer = Customer.create(customerId);
    enablePremiumIfAvailable(customer);
    Order order = Order.create(customer, donutQuantity);
    return orderQueue.add(order);
  }

  private void enablePremiumIfAvailable(Customer customer) {
    if (customer.getId() >= orderConfig.getPremiumCustomerIdLimit()) {
      return;
    }
    customer.enablePremiumPriority();
  }

  @Override
  public Optional<Order> get(int customerId) {
    return orderQueue.get(customerId);
  }

  @Override
  public boolean exists(int customerId) {
    return orderQueue.exists(customerId);
  }

  @Override
  public Optional<Order> cancel(int customerId) {
    Optional<Order> lookup = get(customerId);
    lookup.ifPresent(order -> {
      orderQueue.remove(order);
      order.cancel();
    });
    return lookup;
  }

  @Override
  public Optional<QueueStatus> getQueueStatus(int customerId) {
    Optional<Order> orderLookup = get(customerId);
    if (orderLookup.isEmpty()) {
      return Optional.empty();
    }
    Order order = orderLookup.get();
    return Optional.of(createStatusForOrder(order));
  }

  private QueueStatus createStatusForOrder(Order order) {
    return statusProvider.get(order, lastPickup);
  }

  @Override
  public Optional<Cart> retrieveNextDelivery() {
    Collection<Order> orders = orderQueue.list();
    if (orders.isEmpty()) {
      return Optional.empty();
    }
    Cart cart = pickupDonutsForDelivery(orders);
    LOG.info("Jim has picked up {} donut(s).", cart.getDonutQuantity());
    return Optional.of(cart);
  }

  private Cart pickupDonutsForDelivery(Collection<Order> orders) {
    Cart cart = Cart.create(orderConfig.getMaxDonutsPerCart());
    fillCart(cart, orders);
    markOrdersAsDelivered(cart.getOrders());
    updateLastPickup();
    return cart;
  }

  private void markOrdersAsDelivered(Collection<Order> orders) {
    orderQueue.removeAll(orders);
    orders.forEach(Order::deliver);
  }

  private void updateLastPickup() {
    this.lastPickup = Instant.now();
  }

  private void fillCart(Cart cart, Collection<Order> orders) {
    for (Order order : orders) {
      try {
        cart.addOrder(order);
      } catch (CartFullException cartIsFull) {
        return;
      }
    }
  }

  @Override
  public QueueSnapshot getQueueSnapshot() {
    if (orderQueue.isEmpty()) {
      return QueueSnapshot.create(Instant.now(), Collections.emptyList());
    }
    Collection<QueueStatus> statuses = orderQueue.list()
      .stream()
      .map(this::createStatusForOrder)
      .collect(Collectors.toList());
    return QueueSnapshot.create(Instant.now(), statuses);
  }
}
