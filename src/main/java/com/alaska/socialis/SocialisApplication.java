package com.alaska.socialis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
public class SocialisApplication {

	public static void main(String[] args) {
		SpringApplication.run(SocialisApplication.class, args);
	}

}
