package com.fernando.tareas.controlador;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fernando.tareas.dto.ProyectoRequest;
import com.fernando.tareas.repositorio.ProyectoRepositorio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProyectoControladorTest {

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper json;
    @Autowired private ProyectoRepositorio repositorio;

    @BeforeEach
    void limpiar() {
        repositorio.deleteAll();
    }

    @Test
    void crearProyecto() throws Exception {
        mvc.perform(post("/api/proyectos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(
                        new ProyectoRequest("Mi Proyecto", "Una descripcion"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Mi Proyecto"))
                .andExpect(jsonPath("$.descripcion").value("Una descripcion"))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.fechaCreacion").isNotEmpty());
    }

    @Test
    void crearProyectoSinNombreDevuelve400() throws Exception {
        mvc.perform(post("/api/proyectos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(new ProyectoRequest("", "desc"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores").isArray());
    }

    @Test
    void crearProyectoConNombreNuloDevuelve400() throws Exception {
        mvc.perform(post("/api/proyectos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(new ProyectoRequest(null, "desc"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listarProyectosPaginados() throws Exception {
        for (int i = 1; i <= 15; i++) {
            crearProyectoHelper("Proyecto " + i);
        }
        mvc.perform(get("/api/proyectos?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(10)))
                .andExpect(jsonPath("$.totalElements").value(15))
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    @Test
    void buscarProyectoPorId() throws Exception {
        String respuesta = crearProyectoHelper("Test");
        Long id = json.readTree(respuesta).get("id").asLong();

        mvc.perform(get("/api/proyectos/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Test"));
    }

    @Test
    void buscarProyectoInexistenteDevuelve404() throws Exception {
        mvc.perform(get("/api/proyectos/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value(containsString("999")));
    }

    @Test
    void actualizarProyecto() throws Exception {
        String respuesta = crearProyectoHelper("Viejo");
        Long id = json.readTree(respuesta).get("id").asLong();

        mvc.perform(put("/api/proyectos/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(
                        new ProyectoRequest("Nuevo", "desc actualizada"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Nuevo"))
                .andExpect(jsonPath("$.descripcion").value("desc actualizada"));
    }

    @Test
    void eliminarProyecto() throws Exception {
        String respuesta = crearProyectoHelper("A borrar");
        Long id = json.readTree(respuesta).get("id").asLong();

        mvc.perform(delete("/api/proyectos/" + id))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/proyectos/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    void eliminarProyectoInexistenteDevuelve404() throws Exception {
        mvc.perform(delete("/api/proyectos/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void totalTareasIniciaEnCero() throws Exception {
        String respuesta = crearProyectoHelper("Vacio");
        Long id = json.readTree(respuesta).get("id").asLong();

        mvc.perform(get("/api/proyectos/" + id))
                .andExpect(jsonPath("$.totalTareas").value(0));
    }

    private String crearProyectoHelper(String nombre) throws Exception {
        return mvc.perform(post("/api/proyectos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(
                        new ProyectoRequest(nombre, "desc"))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
    }
}
