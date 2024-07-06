package com.example.Shop;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.awt.*;
import java.net.URI;

@SpringBootApplication
public class ShopApplication {

	public static void main(String[] args) {
		if(GraphicsEnvironment.isHeadless()){
			System.setProperty("java.awt.headless","false");
		}
		SpringApplication.run(ShopApplication.class, args);
	}

	@Bean
	public ApplicationRunner openBrowser() {
		return args -> {
			String url = "http://localhost:8080/product/index";
			openWebpage(url);
		};
	}

	private void openWebpage(String url) {
		try {
			Desktop.getDesktop().browse(new URI(url));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

