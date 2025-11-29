package com.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.server.weather.config.WeatherClientProperties;

@SpringBootApplication
@EnableConfigurationProperties(WeatherClientProperties.class)
public class DemojavaApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemojavaApplication.class, args);
	}

}
