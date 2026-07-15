package com.dtll.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync // Notificaciones SMTP en segundo plano (SRS 2.4)
public class DtllBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(DtllBackendApplication.class, args);
	}

}
