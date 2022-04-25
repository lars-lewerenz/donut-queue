package net.luency.donut.service.order.controller;

import java.net.URI;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Positive;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

import net.luency.donut.service.order.Order;
import net.luency.donut.service.cart.CartDto;
import net.luency.donut.service.order.OrderDto;
import net.luency.donut.service.customer.Customer;
import net.luency.donut.service.order.queue.QueueStatus;
import net.luency.donut.service.order.queue.QueueSnapshot;
import net.luency.donut.service.order.service.OrderService;

@Validated
@RestController
@RequestMapping("v1/orders")
public class OrderController {
  private final OrderService orderService;

  @Autowired
  public OrderController(OrderService orderService) {
    this.orderService = orderService;
  }

  @Operation(summary = "Place a new order in the queue")
  @ApiResponses({
    @ApiResponse(
      responseCode = "201",
      description = "Created order",
      content = @Content(
        mediaType = MediaType.APPLICATION_JSON_VALUE,
        schema = @Schema(implementation = OrderDto.class)
      )
    ),
    @ApiResponse(
      responseCode = "409",
      description = "Customer has already placed an order",
      content = @Content
    )
  })
  @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<OrderDto> create(
    @RequestParam("customer-id") @Min(Customer.MIN_ID) @Max(Customer.MAX_ID) int customerId,
    @RequestParam("donut-quantity") @Positive int donutQuantity
  ) {
    Order order = orderService.create(customerId, donutQuantity);
    URI location = createLocation(customerId);
    return ResponseEntity.created(location).body(order.toDto());
  }

  private URI createLocation(int customerId) {
    ResponseEntity<QueueStatus> statusMethod = MvcUriComponentsBuilder.on(OrderController.class)
      .getQueueStatus(customerId);
    return MvcUriComponentsBuilder.fromMethodCall(statusMethod)
      .build()
      .toUri();
  }

  @Operation(summary = "Cancel a placed order")
  @ApiResponses({
    @ApiResponse(
      responseCode = "204",
      description = "Canceled order",
      content = @Content
    ),
    @ApiResponse(
      responseCode = "404",
      description = "No order found",
      content = @Content
    )
  })
  @DeleteMapping(value = "{customerId}")
  public ResponseEntity<?> cancel(@PathVariable  int customerId) {
    return orderService.cancel(customerId)
      .map(order -> ResponseEntity.noContent().build())
      .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @Operation(summary = "Get a client's status in the queue")
  @ApiResponses({
    @ApiResponse(
      responseCode = "200",
      description = "Found queue status",
      content = @Content(
        mediaType = MediaType.APPLICATION_JSON_VALUE,
        schema = @Schema(implementation = QueueStatus.class)
      )
    ),
    @ApiResponse(
      responseCode = "404",
      description = "No order found",
      content = @Content
    )
  })
  @GetMapping(value = "{customerId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<QueueStatus> getQueueStatus(@PathVariable int customerId) {
    return orderService.getQueueStatus(customerId)
      .map(ResponseEntity::ok)
      .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @Operation(summary = "Fill Jim's cart with his next delivery")
  @ApiResponses({
    @ApiResponse(
      responseCode = "200",
      description = "Found orders which where placed in the cart",
      content = @Content(
        mediaType = MediaType.APPLICATION_JSON_VALUE,
        schema = @Schema(implementation = CartDto.class)
      )
    ),
    @ApiResponse(
      responseCode = "404",
      description = "No orders found",
      content = @Content
    )
  })
  @GetMapping(value = "cart", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<CartDto> retrieveNextDelivery() {
    return orderService.retrieveNextDelivery()
      .map(cart -> ResponseEntity.ok(cart.toDto()))
      .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @Operation(summary = "Get a snapshot of the queue")
  @ApiResponse(
    responseCode = "200",
    description = "Found queue snapshot",
    content = @Content(
      mediaType = MediaType.APPLICATION_JSON_VALUE,
      schema = @Schema(implementation = QueueSnapshot.class)
    )
  )
  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<QueueSnapshot> getQueueSnapshot() {
    return ResponseEntity.ok(orderService.getQueueSnapshot());
  }
}