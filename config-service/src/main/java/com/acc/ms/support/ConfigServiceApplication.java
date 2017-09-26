package com.acc.ms.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.config.server.EnableConfigServer;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
@EnableConfigServer
@EnableDiscoveryClient
public class ConfigServiceApplication {

	private static final Logger LOG = LoggerFactory.getLogger(ConfigServiceApplication.class);
	
	public static void main(String[] args) {	
		
		ConfigurableApplicationContext ctx = SpringApplication.run(ConfigServiceApplication.class, args);

        //LOG.info("Connected to RabbitMQ at: {}", ctx.getEnvironment().getProperty("spring.rabbitmq.host"));
	}
}
