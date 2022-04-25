package net.luency.donut.service.cart;

import lombok.Getter;
import lombok.ToString;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import com.google.common.collect.Lists;

import java.util.Collection;
import java.util.stream.Collectors;

import javax.validation.constraints.Positive;

import net.luency.donut.service.order.Order;
import net.luency.donut.service.order.OrderDto;

@Getter
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(staticName = "create")
public final class Cart {
  @Positive
  private final int capacity;
  private final Collection<Order> orders = Lists.newArrayList();

  public void addOrder(Order order) throws CartFullException {
    if (!hasEnoughSpaceForOrder(order)) {
      throw CartFullException.create(this, order);
    }
    orders.add(order);
  }

  public boolean hasEnoughSpaceForOrder(Order order) {
    int currentQuantity = getDonutQuantity();
    int newQuantity = currentQuantity + order.getDonutQuantity();
    return capacity >= newQuantity;
  }

  public int getDonutQuantity() {
    return orders.stream()
      .mapToInt(Order::getDonutQuantity)
      .sum();
  }

  public boolean contains(Order order) {
    return orders.contains(order);
  }

  public CartDto toDto() {
    Collection<OrderDto> orderDtos = orders.stream()
      .map(Order::toDto)
      .collect(Collectors.toList());
    return CartDto.create(capacity, orderDtos);
  }
}