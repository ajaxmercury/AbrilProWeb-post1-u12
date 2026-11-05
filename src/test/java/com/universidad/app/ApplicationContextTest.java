package com.universidad.app;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Prueba automatizada de arranque del contexto Spring Boot y verificación de Actuator.
 * Opera explícitamente sobre el perfil de desarrollo 'dev' con base de datos H2.
 * Esta clase es ejecutada por el pipeline de CI en GitHub Actions para asegurar
 * que la aplicación esté en condiciones de iniciar antes de construir la imagen Docker.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class ApplicationContextTest {

    @Autowired
    private Environment environment;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("El contexto de Spring Boot debe cargar exitosamente en perfil dev")
    void contextLoads() {
        assertThat(environment.getActiveProfiles()).contains("dev");
    }

    @Test
    @DisplayName("Endpoint Actuator /actuator/health debe responder HTTP 200 con status UP")
    void actuatorHealth_respondeUp() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    @DisplayName("Endpoint raíz / debe responder HTTP 200 y reportar perfil dev activo")
    void endpointRaiz_respondeCorrectamente() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.aplicacion").value("mi-spring-app"))
                .andExpect(jsonPath("$.estado").value("EN_LINEA"))
                .andExpect(jsonPath("$.perfilActivo").value("dev"));
    }
}
