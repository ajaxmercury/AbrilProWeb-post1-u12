package com.universidad.app.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Controlador de demostración y verificación de entorno de ejecución.
 * Expone un endpoint raíz / que reporta el perfil activo y estado general.
 */
@RestController
public class DemoController {

    @Value("${spring.profiles.active:dev}")
    private String perfilActivo;

    @Value("${server.port:8080}")
    private String puerto;

    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> inicio() {
        return ResponseEntity.ok(Map.of(
                "aplicacion", "mi-spring-app",
                "estado", "EN_LINEA",
                "perfilActivo", perfilActivo,
                "puerto", puerto,
                "timestamp", LocalDateTime.now().toString(),
                "mensaje", "Laboratorio U12: Dockerización y CI/CD con GitHub Actions"
        ));
    }
}
