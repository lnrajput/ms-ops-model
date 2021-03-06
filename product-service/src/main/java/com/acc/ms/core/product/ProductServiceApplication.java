package com.acc.ms.core.product;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan({"com.acc.ms.util","com.acc.ms.model"})
public class ProductServiceApplication {

	private static final Logger LOG = LoggerFactory.getLogger(ProductServiceApplication.class);
	
	public static void main(String[] args) {
		ConfigurableApplicationContext ctx = SpringApplication.run(ProductServiceApplication.class, args);
		LOG.info("Connected to RabbitMQ at: {}", ctx.getEnvironment().getProperty("spring.rabbitmq.host"));
	}
}
