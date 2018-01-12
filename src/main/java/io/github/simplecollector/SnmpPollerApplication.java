package io.github.simplecollector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.web.servlet.ErrorPage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import io.github.simplecollector.engine.SnmpPollerEngine;


@SpringBootApplication
@EnableAsync
@EnableScheduling
@Configuration
public class SnmpPollerApplication {
	private static final Logger logger = LoggerFactory.getLogger(SnmpPollerApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(SnmpPollerApplication.class, args);
	}

	@Scheduled(fixedDelay = 60000)
	public void updateConfiguration() {
		logger.info("Just some timer that fires every 60 seconds ... does nothing yet");
	}

	@Bean
	public SnmpPollerEngine snmpPollerEngine() {
	     return new SnmpPollerEngine();
	}
	
	@Bean
    public EmbeddedServletContainerCustomizer containerCustomizer(){
        return new Angular2PathLocationStrategyCustomizer();
    }

    private static class Angular2PathLocationStrategyCustomizer implements EmbeddedServletContainerCustomizer {
        @Override
        public void customize(ConfigurableEmbeddedServletContainer container){
            container.addErrorPages(new ErrorPage(HttpStatus.NOT_FOUND, "/"));
        }
    }
	
 }