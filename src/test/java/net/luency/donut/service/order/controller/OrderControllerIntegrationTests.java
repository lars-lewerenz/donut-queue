package net.luency.donut.service.order.controller;

import java.time.Instant;
import java.util.Optional;
import java.util.Collections;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import net.luency.donut.service.cart.Cart;
import net.luency.donut.service.order.Order;
import net.luency.donut.service.customer.Customer;
import net.luency.donut.service.order.queue.QueueStatus;
import net.luency.donut.service.order.queue.QueueSnapshot;
import net.luency.donut.service.order.service.OrderService;

@WebMvcTest(OrderController.class)
class OrderControllerIntegrationTests {
  @MockBean
  private OrderService orderService;
  @Autowired
  private MockMvc mockMvc;

  @BeforeEach
  void mockOrderService() {
    Order expectedOrder = createOrder();
    QueueStatus expectedStatus = QueueStatus.create(expectedOrder.toDto(), 1, 1);
    when(orderService.create(anyInt(), anyInt())).thenReturn(expectedOrder);
    when(orderService.get(anyInt())).thenReturn(Optional.of(expectedOrder));
    when(orderService.getQueueStatus(anyInt())).thenReturn(Optional.of(expectedStatus));
    when(orderService.getQueueSnapshot()).thenReturn(QueueSnapshot.create(Instant.now(), Collections.emptyList()));
  }

  private Order createOrder() {
    return Order.create(Customer.create(1), 1);
  }

  @Test
  void givenOrder_whenCreating_thenShouldReturnHttpCreated() throws Exception {
    mockMvc.perform(post("/v1/orders/")
      .param("customer-id", "1")
      .param("donut-quantity", "1")
    ).andExpect(status().isCreated());
  }

  @Test
  void givenOrder_whenCreating_thenShouldProvideHeader() throws Exception {
    mockMvc.perform(post("/v1/orders/")
      .param("customer-id", "1")
      .param("donut-quantity", "1")
    ).andExpect(status().isCreated())
      .andExpect(header().exists("Location"))
      .andExpect(header().string("Location", Matchers.containsString("v1/orders/1")));
  }

  @Test
  void givenOrder_whenCreating_thenShouldResponseEqualsOrder() throws Exception {
    mockMvc.perform(post("/v1/orders/")
        .param("customer-id", "1")
        .param("donut-quantity", "1")
      ).andExpect(status().isCreated())
      .andExpect(jsonPath("$.donutQuantity").value(1))
      .andExpect(jsonPath("$.customer.id").value(1));
  }

  @Test
  void givenExceedingCustomerId_whenCreatingOrder_thenShouldReturnHttpBadRequest() throws Exception {
    mockMvc.perform(post("/v1/orders/")
      .param("customer-id", "10000000")
      .param("donut-quantity", "1")
    ).andExpect(status().isBadRequest());
  }

  @Test
  void givenNegativeCustomerId_whenCreatingOrder_thenShouldReturnHttpBadRequest() throws Exception {
    mockMvc.perform(post("/v1/orders/")
      .param("customer-id", "-1")
      .param("donut-quantity", "1")
    ).andExpect(status().isBadRequest());
  }

  @Test
  void givenNegativeDonutQuantity_whenCreatingOrder_thenShouldReturnHttpBadRequest() throws Exception {
    mockMvc.perform(post("/v1/orders/")
      .param("customer-id", "1")
      .param("donut-quantity", "-1")
    ).andExpect(status().isBadRequest());
  }

  @Test
  void givenCustomerId_whenRetrievingStatus_thenShouldReturnHttpOk() throws Exception {
    mockMvc.perform(get("/v1/orders/{customerId}", 1))
      .andExpect(status().isOk());
  }

  @Test
  void givenCustomerId_whenRetrievingNotExistingStatus_thenShouldReturnHttpNotFound() throws Exception {
    when(orderService.getQueueStatus(1)).thenReturn(Optional.empty());
    mockMvc.perform(get("/v1/orders/{customerId}", 1))
      .andExpect(status().isNotFound());
  }

  @Test
  void givenCustomerId_whenCancelingNotExistingOrder_thenShouldReturnHttpNotFound() throws Exception {
    when(orderService.cancel(1)).thenReturn(Optional.empty());
    mockMvc.perform(delete("/v1/orders/{customerId}", 1))
      .andExpect(status().isNotFound());
  }

  @Test
  void givenCustomerId_whenCancelingExistingOrder_thenShouldReturnHttpNoContent() throws Exception {
    when(orderService.cancel(1)).thenReturn(Optional.of(createOrder()));
    mockMvc.perform(delete("/v1/orders/{customerId}", 1))
      .andExpect(status().isNoContent());
  }

  @Test
  void whenRetrievingQueueSnapshot_thenShouldReturnHttpOk() throws Exception {
    mockMvc.perform(get("/v1/orders/"))
      .andExpect(status().isOk());
  }

  @Test
  void givenEmptyCart_whenRetrievingNextDelivery_thenShouldReturnHttpNotFound() throws Exception {
    when(orderService.retrieveNextDelivery()).thenReturn(Optional.empty());

    mockMvc.perform(get("/v1/orders/cart/"))
      .andExpect(status().isNotFound());
  }

  @Test
  void givenCart_whenRetrievingNextDelivery_thenShouldReturnHttpNotFound() throws Exception {
    when(orderService.retrieveNextDelivery()).thenReturn(Optional.of(Cart.create(1)));

    mockMvc.perform(get("/v1/orders/cart/"))
      .andExpect(status().isOk());
  }
}