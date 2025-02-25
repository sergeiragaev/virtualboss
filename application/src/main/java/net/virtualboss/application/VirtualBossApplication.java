package net.virtualboss.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "net.virtualboss")
@EntityScan(basePackages = {"net.virtualboss.common"})
@EnableJpaRepositories(basePackages = {"net.virtualboss.common"})
@EnableCaching
public class VirtualBossApplication {

	public static void main(String[] args) {
		SpringApplication.run(VirtualBossApplication.class, args);
	}

}
