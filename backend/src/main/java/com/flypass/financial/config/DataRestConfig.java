package com.flypass.financial.config;

import com.flypass.financial.entity.Account;
import com.flypass.financial.entity.Customer;
import com.flypass.financial.entity.Transaction;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

@Configuration
public class DataRestConfig {

    @Bean
    public RepositoryRestConfigurer repositoryRestConfigurer() {
        return new RepositoryRestConfigurer() {
            @Override
            public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config, CorsRegistry cors) {
                config.exposeIdsFor(Customer.class, Account.class, Transaction.class);

                config.getExposureConfiguration()
                        .forDomainType(Customer.class)
                        .withCollectionExposure((metadata, methods) ->
                                methods.disable(HttpMethod.POST, HttpMethod.PUT, HttpMethod.PATCH))
                        .withItemExposure((metadata, methods) ->
                                methods.disable(HttpMethod.POST, HttpMethod.PUT, HttpMethod.PATCH));

                config.getExposureConfiguration()
                        .forDomainType(Account.class)
                        .withCollectionExposure((metadata, methods) ->
                                methods.disable(HttpMethod.POST, HttpMethod.PUT, HttpMethod.PATCH))
                        .withItemExposure((metadata, methods) ->
                                methods.disable(HttpMethod.POST, HttpMethod.PUT, HttpMethod.PATCH));

                config.getExposureConfiguration()
                        .forDomainType(Transaction.class)
                        .withCollectionExposure((metadata, methods) ->
                                methods.disable(HttpMethod.POST, HttpMethod.PUT, HttpMethod.PATCH))
                        .withItemExposure((metadata, methods) ->
                                methods.disable(HttpMethod.POST, HttpMethod.PUT, HttpMethod.PATCH));

                cors.addMapping("/api/v1/**")
                        .allowedOrigins("*")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                        .allowedHeaders("*");
            }
        };
    }
}
