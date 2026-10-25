package com.fernando.tareas.dto;

import com.fernando.tareas.modelo.Proyecto;
import java.time.LocalDateTime;

public record ProyectoResponse(
        Long id,
        String nombre,
        String descripcion,
        LocalDateTime fechaCreacion,
        int totalTareas
) {
    public static ProyectoResponse desde(Proyecto proyecto) {
        return new ProyectoResponse(
                proyecto.getId(),
                proyecto.getNombre(),
                proyecto.getDescripcion(),
                proyecto.getFechaCreacion(),
                proyecto.getTareas().size()
        );
    }
}
