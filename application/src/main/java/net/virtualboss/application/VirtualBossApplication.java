package net.virtualboss.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO;

@SpringBootApplication(scanBasePackages = "net.virtualboss")
@EntityScan(basePackages = {"net.virtualboss.common"})
@EnableJpaRepositories(basePackages = {"net.virtualboss.common"})
@EnableCaching
@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)
public class VirtualBossApplication {

	public static void main(String[] args) {
		SpringApplication.run(VirtualBossApplication.class, args);
	}

}
