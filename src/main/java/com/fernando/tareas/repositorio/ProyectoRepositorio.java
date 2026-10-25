package com.fernando.tareas.repositorio;

import com.fernando.tareas.modelo.Proyecto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProyectoRepositorio extends JpaRepository<Proyecto, Long> {
}
