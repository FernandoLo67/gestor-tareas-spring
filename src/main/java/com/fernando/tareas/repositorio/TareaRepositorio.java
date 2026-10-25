package com.fernando.tareas.repositorio;

import com.fernando.tareas.modelo.EstadoTarea;
import com.fernando.tareas.modelo.Prioridad;
import com.fernando.tareas.modelo.Tarea;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TareaRepositorio extends JpaRepository<Tarea, Long> {

    Page<Tarea> findByProyectoId(Long proyectoId, Pageable pageable);

    Page<Tarea> findByProyectoIdAndEstado(Long proyectoId, EstadoTarea estado, Pageable pageable);

    Page<Tarea> findByProyectoIdAndPrioridad(Long proyectoId, Prioridad prioridad, Pageable pageable);

    Page<Tarea> findByProyectoIdAndEstadoAndPrioridad(
            Long proyectoId, EstadoTarea estado, Prioridad prioridad, Pageable pageable);
}
