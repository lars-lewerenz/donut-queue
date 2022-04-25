package net.luency.donut.service.order;

import lombok.Getter;
import lombok.AllArgsConstructor;

import java.time.Instant;
import java.io.Serializable;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PastOrPresent;

import net.luency.donut.service.customer.CustomerDto;

@Getter
@AllArgsConstructor(staticName = "create")
public final class OrderDto implements Serializable {
  private static final long serialVersionUID = -3968823124174833966L;

  @NotNull
  private final CustomerDto customer;
  @PastOrPresent
  private final Instant creation;
  @Positive
  private final int donutQuantity;
  @NotNull
  private final Order.Status status;
}
