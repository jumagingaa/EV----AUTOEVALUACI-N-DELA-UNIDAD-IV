package ec.edu.uteq.appweb.biblioteca.web;

import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ec.edu.uteq.appweb.biblioteca.BaseIntegracionTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

class LibroControllerIT extends BaseIntegracionTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(username = "lector", roles = "LECTOR")
    @DisplayName("GET libros devuelve 200, cinco campos y paginacion correcta")
    void listarLibrosDevuelveEnvoltorio() throws Exception {

        mockMvc.perform(get("/api/v1/libros")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", aMapWithSize(5)))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.message").value("Libros listados"))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.meta").isMap())
                .andExpect(jsonPath("$.meta.page").value(0))
                .andExpect(jsonPath("$.meta.size").value(5))
                .andExpect(jsonPath("$.meta.totalElements").isNumber());
    }

    @Test
    @WithMockUser(username = "lector", roles = "LECTOR")
    @DisplayName("GET libro inexistente devuelve 404 con Problem Details")
    void libroInexistenteDevuelveProblemDetail() throws Exception {

        mockMvc.perform(get("/api/v1/libros/999999"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title")
                        .value("Recurso no encontrado"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").isNotEmpty());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("POST libro con titulo vacio devuelve 400 y errores de validacion")
    void crearLibroConTituloVacioDevuelve400() throws Exception {

        String solicitud = """
                {
                  "isbn": "9780132350884",
                  "titulo": "",
                  "anioPublicacion": 2008,
                  "ejemplaresTotales": 5,
                  "autorId": 1,
                  "editorialId": 1,
                  "categoriaId": 1
                }
                """;

        mockMvc.perform(post("/api/v1/libros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(solicitud))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors").isNotEmpty())
                .andExpect(jsonPath("$.errors",
                        hasItem(containsString("titulo"))));
    }
}