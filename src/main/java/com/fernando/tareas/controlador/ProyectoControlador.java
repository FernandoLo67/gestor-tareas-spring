package com.fernando.tareas.controlador;

import com.fernando.tareas.dto.ProyectoRequest;
import com.fernando.tareas.dto.ProyectoResponse;
import com.fernando.tareas.servicio.ProyectoServicio;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/proyectos")
public class ProyectoControlador {

    private final ProyectoServicio servicio;

    public ProyectoControlador(ProyectoServicio servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public Page<ProyectoResponse> listar(Pageable pageable) {
        return servicio.listar(pageable);
    }

    @GetMapping("/{id}")
    public ProyectoResponse buscar(@PathVariable Long id) {
        return servicio.buscarPorId(id);
    }

    @PostMapping
    public ResponseEntity<ProyectoResponse> crear(@Valid @RequestBody ProyectoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(servicio.crear(request));
    }

    @PutMapping("/{id}")
    public ProyectoResponse actualizar(@PathVariable Long id,
                                       @Valid @RequestBody ProyectoRequest request) {
        return servicio.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        servicio.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
