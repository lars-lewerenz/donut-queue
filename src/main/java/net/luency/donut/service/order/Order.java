package net.luency.donut.service.order;

import lombok.Getter;
import lombok.ToString;
import lombok.EqualsAndHashCode;

import java.time.Instant;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PastOrPresent;

import net.luency.donut.service.customer.Customer;

@Getter
@ToString
// it doesn't matter when the order was placed since only one order can be placed
// per customer
@EqualsAndHashCode(exclude = "creation")
public final class Order implements Comparable<Order> {

  @NotNull
  private final Customer customer;
  @PastOrPresent
  private final Instant creation;
  @Positive
  private final int donutQuantity;
  @NotNull
  private Status status;

  private Order(Customer customer, int donutQuantity) {
    this.customer = customer;
    this.donutQuantity = donutQuantity;
    this.status = Status.PENDING;
    this.creation = Instant.now();
  }

  public void deliver() {
    this.status = Status.DELIVERED;
  }

  public void cancel() {
    this.status = Status.CANCELLED;
  }

  @Override
  public int compareTo(Order other) {
    // should sort descending
    return other.getCustomer().getType().equals(customer.getType())
      ? other.getCreation().compareTo(creation)
      : other.getCustomer().compareTo(customer);
  }

  public static Order create(Customer customer, int donutQuantity) {
    return new Order(customer, donutQuantity);
  }

  public OrderDto toDto() {
    return OrderDto.create(customer.toDto(), creation, donutQuantity, status);
  }

  public enum Status {
    PENDING,
    DELIVERED,
    CANCELLED
  }
}