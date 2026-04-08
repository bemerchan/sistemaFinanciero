package com.flypass.financial.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Mini Sistema Financiero - Flypass API")
                        .description("""
                                API REST para gestión de clientes, cuentas bancarias y transacciones.
                                
                                ## Funcionalidades
                                - **Clientes**: CRUD completo con validación de mayoría de edad
                                - **Cuentas**: Ahorro (53XXXXXXXX) y Corriente (33XXXXXXXX)
                                - **Transacciones**: Consignaciones y retiros con actualización automática de saldo
                                
                                ## Reglas de negocio
                                - No se permiten clientes menores de 18 años
                                - No se pueden eliminar clientes con cuentas vinculadas
                                - Saldo mínimo de $0 en cuentas de ahorro
                                - Número de cuenta autogenerado único de 10 dígitos
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Flypass - Equipo de Desarrollo")
                                .email("dev@flypass.com"))
                        .license(new License()
                                .name("MIT")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:9000")
                                .description("Servidor de desarrollo local")
                ));
    }
}
