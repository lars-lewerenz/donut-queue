package net.luency.donut.service.customer;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

@Getter
@RequiredArgsConstructor(staticName = "create")
public final class CustomerDto implements Serializable {
  private static final long serialVersionUID = 2403719044957559130L;

  @Size(min = Customer.MIN_ID, max = Customer.MAX_ID)
  private final int id;
  @NotNull
  private final Customer.Type type;
}