package com.acc.ms.support;

import java.security.Principal;

import javax.net.ssl.HttpsURLConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@EnableResourceServer
@EnableAuthorizationServer
@RestController
public class AuthServiceApplication {

	private static final Logger LOG = LoggerFactory.getLogger(AuthServiceApplication.class);

	@RequestMapping("/user")
	public Principal user(Principal user) {
		return user;
	}
	
	static {
		// for localhost testing only
		
		LOG.warn("Will now disable hostname check in SSL, only to be used during development");
		HttpsURLConnection.setDefaultHostnameVerifier((hostname, sslSession) -> true);
	}	
	public static void main(String[] args) {
		SpringApplication.run(AuthServiceApplication.class, args);
	}
}
