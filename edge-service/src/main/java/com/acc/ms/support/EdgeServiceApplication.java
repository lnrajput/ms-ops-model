package com.acc.ms.support;

import javax.net.ssl.HttpsURLConnection;
import javax.servlet.Filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.stereotype.Controller;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

@SpringBootApplication
@Controller
@EnableZuulProxy
@EnableResourceServer
public class EdgeServiceApplication {

    private static final Logger LOG = LoggerFactory.getLogger(EdgeServiceApplication.class);

    static {
        // for localhost testing only
        LOG.warn("Will now disable hostname check in SSL, only to be used during development");
        HttpsURLConnection.setDefaultHostnameVerifier((hostname, sslSession) -> true);
    }

    @Bean
    public Filter logFilter() {
        // TODO: Extract bean-def to Util-component!
        CommonsRequestLoggingFilter filter = new CommonsRequestLoggingFilter();
        filter.setIncludeQueryString(true);
        filter.setIncludePayload(true);
        filter.setMaxPayloadLength(5120);
        return filter;
    }

    public static void main(String[] args) {
        int buildNo = 15;
        LOG.info("Edge-server, starting build no. {}...", buildNo);

        new SpringApplicationBuilder(EdgeServiceApplication.class).web(true).run(args);

        LOG.info("Edge-server, build no. {} started", buildNo);
    }	
}
