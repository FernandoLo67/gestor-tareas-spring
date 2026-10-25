package com.fernando.tareas.controlador;

import com.fernando.tareas.dto.CambioEstadoRequest;
import com.fernando.tareas.dto.TareaRequest;
import com.fernando.tareas.dto.TareaResponse;
import com.fernando.tareas.modelo.EstadoTarea;
import com.fernando.tareas.modelo.Prioridad;
import com.fernando.tareas.servicio.TareaServicio;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class TareaControlador {

    private final TareaServicio servicio;

    public TareaControlador(TareaServicio servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/proyectos/{proyectoId}/tareas")
    public Page<TareaResponse> listar(
            @PathVariable Long proyectoId,
            @RequestParam(required = false) EstadoTarea estado,
            @RequestParam(required = false) Prioridad prioridad,
            Pageable pageable) {
        return servicio.listarPorProyecto(proyectoId, estado, prioridad, pageable);
    }

    @GetMapping("/tareas/{id}")
    public TareaResponse buscar(@PathVariable Long id) {
        return servicio.buscarPorId(id);
    }

    @PostMapping("/proyectos/{proyectoId}/tareas")
    public ResponseEntity<TareaResponse> crear(
            @PathVariable Long proyectoId,
            @Valid @RequestBody TareaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(servicio.crear(proyectoId, request));
    }

    @PutMapping("/tareas/{id}")
    public TareaResponse actualizar(@PathVariable Long id,
                                    @Valid @RequestBody TareaRequest request) {
        return servicio.actualizar(id, request);
    }

    @PatchMapping("/tareas/{id}/estado")
    public TareaResponse cambiarEstado(@PathVariable Long id,
                                       @Valid @RequestBody CambioEstadoRequest request) {
        return servicio.cambiarEstado(id, request.estado());
    }

    @DeleteMapping("/tareas/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        servicio.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
