package net.luency.donut.service.config;

import lombok.Getter;
import lombok.Setter;

import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "donut")
public class OrderConfig {

  private int premiumCustomerIdLimit;
  private int maxDonutsPerCart;
  private int maxDonutsPerOrder;
  private long orderScheduleInterval;
}
