package net.luency.donut.service.order.queue;

import lombok.Getter;
import lombok.ToString;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.io.Serializable;
import java.util.Collection;

@Getter
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(staticName = "create")
public final class QueueSnapshot implements Serializable {
  private static final long serialVersionUID = -552247221594062252L;

  private final Instant timestamp;
  private final Collection<QueueStatus> statuses;
}