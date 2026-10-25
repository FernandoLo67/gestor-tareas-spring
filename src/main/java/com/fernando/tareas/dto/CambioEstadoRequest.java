package com.fernando.tareas.dto;

import com.fernando.tareas.modelo.EstadoTarea;
import jakarta.validation.constraints.NotNull;

public record CambioEstadoRequest(
        @NotNull(message = "El estado es obligatorio")
        EstadoTarea estado
) {}
