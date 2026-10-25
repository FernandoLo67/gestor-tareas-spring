package com.fernando.tareas.dto;

import com.fernando.tareas.modelo.Prioridad;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record TareaRequest(
        @NotBlank(message = "El titulo es obligatorio")
        @Size(max = 200, message = "El titulo no puede superar los 200 caracteres")
        String titulo,

        @Size(max = 1000, message = "La descripcion no puede superar los 1000 caracteres")
        String descripcion,

        Prioridad prioridad,

        LocalDate fechaLimite
) {}
