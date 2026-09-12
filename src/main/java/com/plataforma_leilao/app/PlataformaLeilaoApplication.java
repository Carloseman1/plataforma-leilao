package com.plataforma_leilao.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PlataformaLeilaoApplication {

	public static void main(String[] args) {
		SpringApplication.run(PlataformaLeilaoApplication.class, args);
	}

}
