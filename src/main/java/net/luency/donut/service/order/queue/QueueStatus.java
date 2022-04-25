package net.luency.donut.service.order.queue;

import lombok.Getter;
import lombok.ToString;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import com.google.common.base.Preconditions;

import java.io.Serializable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

import net.luency.donut.service.order.OrderDto;

@Getter
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class QueueStatus implements Serializable {
  private static final long serialVersionUID = -1176631711761744620L;

  @NotNull
  private final OrderDto order;
  @Positive
  private final int place;
  @Positive
  private final long approximateWaitingTime;

  public static QueueStatus create(OrderDto order, int place, long approximateWaitingTime) {
    Preconditions.checkNotNull(order, "order must not be null");
    Preconditions.checkArgument(place > 0, "place must be positive");
    Preconditions.checkArgument(approximateWaitingTime >= 0, "approximateWaitingTime must not be negative");
    return new QueueStatus(order, place, approximateWaitingTime);
  }
}