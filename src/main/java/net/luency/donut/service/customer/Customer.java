package net.luency.donut.service.customer;

import lombok.Getter;
import lombok.ToString;
import lombok.EqualsAndHashCode;

import com.google.common.base.Preconditions;

import javax.validation.constraints.Size;
import javax.validation.constraints.NotNull;

@Getter
@ToString
@EqualsAndHashCode
public final class Customer implements Comparable<Customer> {
  public static final int MIN_ID = 1;
  public static final int MAX_ID = 20_000;

  @Size(min = MIN_ID, max = MAX_ID)
  private final int id;
  @NotNull
  private Type type;

  private Customer(int id) {
    this.id = id;
    this.type = Type.NORMAL;
  }

  public void enablePremiumPriority() {
    this.type = Type.PREMIUM;
  }

  @Override
  public int compareTo(Customer other) {
    return Integer.compare(other.getType().getPriority(), type.getPriority());
  }

  public static Customer create(int id) {
    Preconditions.checkArgument(id >= MIN_ID && id <= MAX_ID, "id");
    return new Customer(id);
  }

  public CustomerDto toDto() {
    return CustomerDto.create(id, type);
  }

  public enum Type {
    NORMAL(1),
    PREMIUM(10);

    private final int priority;

    Type(int priority) {
      this.priority = priority;
    }

    public int getPriority() {
      return priority;
    }
  }
}
