package com.cuidadodemascotas.microservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
//(exclude = {SecurityAutoConfiguration.class})

@SpringBootApplication
@EntityScan(basePackages = "org.example.cuidadodemascota.commons.entities")
@EnableCaching
@EnableDiscoveryClient
public class MicroserviceApplication extends SpringBootServletInitializer {
	public static void main(String[] args) {

        SpringApplication.run(MicroserviceApplication.class, args);

	}

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(MicroserviceApplication.class);
    }
}
