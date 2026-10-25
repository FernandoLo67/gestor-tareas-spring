package com.fernando.tareas.servicio;

import com.fernando.tareas.dto.ProyectoRequest;
import com.fernando.tareas.dto.ProyectoResponse;
import com.fernando.tareas.excepcion.RecursoNoEncontradoException;
import com.fernando.tareas.modelo.Proyecto;
import com.fernando.tareas.repositorio.ProyectoRepositorio;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProyectoServicio {

    private final ProyectoRepositorio repositorio;

    public ProyectoServicio(ProyectoRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    @Transactional(readOnly = true)
    public Page<ProyectoResponse> listar(Pageable pageable) {
        return repositorio.findAll(pageable).map(ProyectoResponse::desde);
    }

    @Transactional(readOnly = true)
    public ProyectoResponse buscarPorId(Long id) {
        return ProyectoResponse.desde(buscarEntidad(id));
    }

    @Transactional
    public ProyectoResponse crear(ProyectoRequest request) {
        Proyecto proyecto = new Proyecto(request.nombre(), request.descripcion());
        return ProyectoResponse.desde(repositorio.save(proyecto));
    }

    @Transactional
    public ProyectoResponse actualizar(Long id, ProyectoRequest request) {
        Proyecto proyecto = buscarEntidad(id);
        proyecto.setNombre(request.nombre());
        proyecto.setDescripcion(request.descripcion());
        return ProyectoResponse.desde(repositorio.save(proyecto));
    }

    @Transactional
    public void eliminar(Long id) {
        Proyecto proyecto = buscarEntidad(id);
        repositorio.delete(proyecto);
    }

    Proyecto buscarEntidad(Long id) {
        return repositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Proyecto no encontrado con id: " + id));
    }
}
