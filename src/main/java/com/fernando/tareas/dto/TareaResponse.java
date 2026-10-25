package com.fernando.tareas.dto;

import com.fernando.tareas.modelo.EstadoTarea;
import com.fernando.tareas.modelo.Prioridad;
import com.fernando.tareas.modelo.Tarea;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record TareaResponse(
        Long id,
        String titulo,
        String descripcion,
        EstadoTarea estado,
        Prioridad prioridad,
        LocalDateTime fechaCreacion,
        LocalDate fechaLimite,
        Long proyectoId
) {
    public static TareaResponse desde(Tarea tarea) {
        return new TareaResponse(
                tarea.getId(),
                tarea.getTitulo(),
                tarea.getDescripcion(),
                tarea.getEstado(),
                tarea.getPrioridad(),
                tarea.getFechaCreacion(),
                tarea.getFechaLimite(),
                tarea.getProyecto().getId()
        );
    }
}
