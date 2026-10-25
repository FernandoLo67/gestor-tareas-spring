package com.fernando.tareas.controlador;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fernando.tareas.dto.CambioEstadoRequest;
import com.fernando.tareas.dto.ProyectoRequest;
import com.fernando.tareas.dto.TareaRequest;
import com.fernando.tareas.modelo.EstadoTarea;
import com.fernando.tareas.modelo.Prioridad;
import com.fernando.tareas.repositorio.ProyectoRepositorio;
import com.fernando.tareas.repositorio.TareaRepositorio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TareaControladorTest {

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper json;
    @Autowired private ProyectoRepositorio proyectoRepo;
    @Autowired private TareaRepositorio tareaRepo;

    private Long proyectoId;

    @BeforeEach
    void preparar() throws Exception {
        tareaRepo.deleteAll();
        proyectoRepo.deleteAll();
        String resp = mvc.perform(post("/api/proyectos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(
                        new ProyectoRequest("Proyecto Test", "desc"))))
                .andReturn().getResponse().getContentAsString();
        proyectoId = json.readTree(resp).get("id").asLong();
    }

    @Test
    void crearTarea() throws Exception {
        mvc.perform(post("/api/proyectos/" + proyectoId + "/tareas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(new TareaRequest(
                        "Hacer algo", "detalle", Prioridad.ALTA,
                        LocalDate.of(2026, 12, 31)))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.titulo").value("Hacer algo"))
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.prioridad").value("ALTA"))
                .andExpect(jsonPath("$.proyectoId").value(proyectoId));
    }

    @Test
    void crearTareaSinTituloDevuelve400() throws Exception {
        mvc.perform(post("/api/proyectos/" + proyectoId + "/tareas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(
                        new TareaRequest("", null, null, null))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void crearTareaEnProyectoInexistenteDevuelve404() throws Exception {
        mvc.perform(post("/api/proyectos/999/tareas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(
                        new TareaRequest("Tarea", null, null, null))))
                .andExpect(status().isNotFound());
    }

    @Test
    void prioridadPorDefectoEsMedia() throws Exception {
        mvc.perform(post("/api/proyectos/" + proyectoId + "/tareas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(
                        new TareaRequest("Sin prioridad", null, null, null))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.prioridad").value("MEDIA"));
    }

    @Test
    void listarTareasPaginadas() throws Exception {
        for (int i = 1; i <= 12; i++) {
            crearTareaHelper("Tarea " + i);
        }
        mvc.perform(get("/api/proyectos/" + proyectoId + "/tareas?page=0&size=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(5)))
                .andExpect(jsonPath("$.totalElements").value(12));
    }

    @Test
    void filtrarPorEstado() throws Exception {
        Long id = crearYObtenerIdTarea("Tarea A");
        crearTareaHelper("Tarea B");

        mvc.perform(patch("/api/tareas/" + id + "/estado")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(
                        new CambioEstadoRequest(EstadoTarea.COMPLETADA))));

        mvc.perform(get("/api/proyectos/" + proyectoId
                + "/tareas?estado=PENDIENTE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].titulo").value("Tarea B"));
    }

    @Test
    void filtrarPorPrioridad() throws Exception {
        mvc.perform(post("/api/proyectos/" + proyectoId + "/tareas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(
                        new TareaRequest("Alta", null, Prioridad.ALTA, null))));
        crearTareaHelper("Media");

        mvc.perform(get("/api/proyectos/" + proyectoId
                + "/tareas?prioridad=ALTA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].titulo").value("Alta"));
    }

    @Test
    void buscarTareaPorId() throws Exception {
        Long id = crearYObtenerIdTarea("Buscar esta");
        mvc.perform(get("/api/tareas/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Buscar esta"));
    }

    @Test
    void actualizarTarea() throws Exception {
        Long id = crearYObtenerIdTarea("Vieja");
        mvc.perform(put("/api/tareas/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(
                        new TareaRequest("Nueva", "actualizada",
                                Prioridad.BAJA, null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Nueva"))
                .andExpect(jsonPath("$.prioridad").value("BAJA"));
    }

    @Test
    void cambiarEstado() throws Exception {
        Long id = crearYObtenerIdTarea("Pendiente");
        mvc.perform(patch("/api/tareas/" + id + "/estado")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(
                        new CambioEstadoRequest(EstadoTarea.EN_PROGRESO))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("EN_PROGRESO"));
    }

    @Test
    void eliminarTarea() throws Exception {
        Long id = crearYObtenerIdTarea("A borrar");
        mvc.perform(delete("/api/tareas/" + id))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/tareas/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    void eliminarProyectoEliminaSusTareas() throws Exception {
        crearTareaHelper("Tarea del proyecto");
        mvc.perform(delete("/api/proyectos/" + proyectoId))
                .andExpect(status().isNoContent());
        assertEquals(0, tareaRepo.count());
    }

    @Test
    void totalTareasSeActualiza() throws Exception {
        crearTareaHelper("Una");
        crearTareaHelper("Dos");
        mvc.perform(get("/api/proyectos/" + proyectoId))
                .andExpect(jsonPath("$.totalTareas").value(2));
    }

    private void crearTareaHelper(String titulo) throws Exception {
        mvc.perform(post("/api/proyectos/" + proyectoId + "/tareas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(
                        new TareaRequest(titulo, null, null, null))))
                .andExpect(status().isCreated());
    }

    private Long crearYObtenerIdTarea(String titulo) throws Exception {
        String resp = mvc.perform(post("/api/proyectos/" + proyectoId + "/tareas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(
                        new TareaRequest(titulo, null, null, null))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return json.readTree(resp).get("id").asLong();
    }
}
