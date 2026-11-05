package com.universidad.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Clase principal de inicio de la aplicación Spring Boot.
 * Diseñada para ejecución conteinerizada en Docker con soporte de perfiles
 * externalizados (dev, prod) e integración con Actuator para monitoreo de salud.
 */
@SpringBootApplication
public class MiSpringApplication {

    public static void main(String[] args) {
        SpringApplication.run(MiSpringApplication.class, args);
    }
}
