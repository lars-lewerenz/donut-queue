package net.luency.donut.service.cart;

import lombok.Getter;
import lombok.ToString;
import lombok.RequiredArgsConstructor;

import net.luency.donut.service.order.Order;

@Getter
@ToString
@RequiredArgsConstructor(staticName = "create")
public final class CartFullException extends Exception{
  private static final long serialVersionUID = -3819719750258448944L;

  private final Cart cart;
  private final Order order;
}