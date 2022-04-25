package net.luency.donut.service.exception;

import lombok.Getter;
import lombok.Builder;
import lombok.ToString;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.Collection;
import java.io.Serializable;

@Getter
@ToString
@Builder(setterPrefix = "set", builderMethodName = "newBuilder")
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ExceptionDetails implements Serializable {
  private static final long serialVersionUID = -7689953305812888966L;

  private final Instant timestamp;
  private final int status;
  private final Collection<String> errors;
  private final String message;
  private final String path;
}