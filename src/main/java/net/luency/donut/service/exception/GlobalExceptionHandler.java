package net.luency.donut.service.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.time.Instant;
import java.util.stream.Collectors;
import javax.validation.ConstraintViolationException;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
  private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  private static final String ERROR_FORMAT = "%s: %s";

  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
    MethodArgumentNotValidException invalidArgumentFailure,
    HttpHeaders headers,
    HttpStatus status,
    WebRequest request
  ) {
    Collection<String> validationErrors = invalidArgumentFailure.getBindingResult()
      .getFieldErrors()
      .stream()
      .map(error -> String.format(ERROR_FORMAT, error.getField(), error.getDefaultMessage()))
      .collect(Collectors.toList());
    return createExceptionResponseEntityAndLog(HttpStatus.BAD_REQUEST, request, validationErrors);
  }

  @Override
  protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(
    HttpRequestMethodNotSupportedException invalidMethodFailure,
    HttpHeaders headers,
    HttpStatus status,
    WebRequest request
  ) {
    return createExceptionResponseEntityAndLog(invalidMethodFailure, request, HttpStatus.METHOD_NOT_ALLOWED);
  }

  @Override
  protected ResponseEntity<Object> handleMissingPathVariable(
    MissingPathVariableException missingPathVariableFailure,
    HttpHeaders headers,
    HttpStatus status,
    WebRequest request
  ) {
    return createExceptionResponseEntityAndLog(missingPathVariableFailure, request, HttpStatus.BAD_REQUEST);
  }

  @Override
  protected ResponseEntity<Object> handleMissingServletRequestParameter(
    MissingServletRequestParameterException missingParameterFailure,
    HttpHeaders headers,
    HttpStatus status,
    WebRequest request
  ) {
    return createExceptionResponseEntityAndLog(missingParameterFailure, request, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<Object> handleArgumentViolation(
    IllegalArgumentException invalidInputFailure,
    WebRequest request
  ) {
    return createExceptionResponseEntityAndLog(invalidInputFailure, request, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<Object> handleConstraintViolation(
    ConstraintViolationException invalidInputFailure,
    WebRequest request
  ) {
    Collection<String> validationErrors = invalidInputFailure.getConstraintViolations()
      .stream()
      .map(violation -> String.format(ERROR_FORMAT, violation.getPropertyPath(), violation.getMessage()))
      .collect(Collectors.toList());
    return createExceptionResponseEntityAndLog(HttpStatus.BAD_REQUEST, request, validationErrors);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Object> handleAllExceptions(
    Exception failure,
    WebRequest request
  ) {
    ResponseStatus responseStatus = failure.getClass().getAnnotation(ResponseStatus.class);
    HttpStatus status = responseStatus != null ? responseStatus.value() : HttpStatus.INTERNAL_SERVER_ERROR;
    return createExceptionResponseEntityAndLog(failure, request, status);
  }

  private ResponseEntity<Object> createExceptionResponseEntityAndLog(Exception failure, WebRequest request, HttpStatus status) {
    String localizedMessage = failure.getLocalizedMessage();
    String message = StringUtils.hasText(localizedMessage) ? localizedMessage : status.getReasonPhrase();
    return createExceptionResponseEntityAndLog(status, request, Collections.singletonList(message));
  }

  private ResponseEntity<Object> createExceptionResponseEntityAndLog(
    HttpStatus status,
    WebRequest request,
    Collection<String> errors
  ) {
    ExceptionDetails response = ExceptionDetails.newBuilder()
      .setTimestamp(Instant.now())
      .setStatus(status.value())
      .setErrors(errors)
      .setMessage(status.getReasonPhrase())
      .setPath(request.getDescription(false))
      .build();
    logErrors(response);
    return ResponseEntity.status(status).body(response);
  }

  private void logErrors(ExceptionDetails response) {
    Collection<String> errors = response.getErrors();
    String errorMessage = !CollectionUtils.isEmpty(errors)
      ? errors.stream().filter(StringUtils::hasText).collect(Collectors.joining(", "))
      : response.getMessage();
    LOG.error("Got errors for path {}: {}", response.getPath(), errorMessage);
  }
}