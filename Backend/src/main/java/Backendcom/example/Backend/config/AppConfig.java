package Backendcom.example.Backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configuración de aplicación
 */
@Configuration
public class AppConfig {
    
    /**
     * Bean para RestTemplate
     * Utilizado para hacer peticiones HTTP a APIs externas (SUNAT)
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
