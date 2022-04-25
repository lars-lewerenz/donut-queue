package net.luency.donut.service.cart;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;
import java.util.Collection;
import javax.validation.constraints.Positive;

import net.luency.donut.service.order.OrderDto;

@Getter
@RequiredArgsConstructor(staticName = "create")
public final class CartDto implements Serializable {
  private static final long serialVersionUID = -1944303153472048554L;

  @Positive
  private final int capacity;
  private final Collection<OrderDto> orders;
}
