package com.danielabirzan.storemanagement;

import com.danielabirzan.storemanagement.security.SecurityCredentials;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO;

@SecurityScheme(
		name = "bearerAuth",
		type = SecuritySchemeType.HTTP,
		scheme = "bearer",
		bearerFormat = "JWT"
)
@OpenAPIDefinition(
		security = @SecurityRequirement(name = "bearerAuth")
)
@EnableConfigurationProperties(SecurityCredentials.class)
@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)
@SpringBootApplication
public class StoreManagementApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(StoreManagementApiApplication.class, args);
	}

}
