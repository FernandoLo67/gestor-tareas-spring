package com.fernando.tareas.servicio;

import com.fernando.tareas.dto.TareaRequest;
import com.fernando.tareas.dto.TareaResponse;
import com.fernando.tareas.excepcion.RecursoNoEncontradoException;
import com.fernando.tareas.modelo.EstadoTarea;
import com.fernando.tareas.modelo.Prioridad;
import com.fernando.tareas.modelo.Proyecto;
import com.fernando.tareas.modelo.Tarea;
import com.fernando.tareas.repositorio.TareaRepositorio;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TareaServicio {

    private final TareaRepositorio repositorio;
    private final ProyectoServicio proyectoServicio;

    public TareaServicio(TareaRepositorio repositorio, ProyectoServicio proyectoServicio) {
        this.repositorio = repositorio;
        this.proyectoServicio = proyectoServicio;
    }

    @Transactional(readOnly = true)
    public Page<TareaResponse> listarPorProyecto(Long proyectoId, EstadoTarea estado,
                                                  Prioridad prioridad, Pageable pageable) {
        Page<Tarea> pagina;
        if (estado != null && prioridad != null) {
            pagina = repositorio.findByProyectoIdAndEstadoAndPrioridad(
                    proyectoId, estado, prioridad, pageable);
        } else if (estado != null) {
            pagina = repositorio.findByProyectoIdAndEstado(proyectoId, estado, pageable);
        } else if (prioridad != null) {
            pagina = repositorio.findByProyectoIdAndPrioridad(proyectoId, prioridad, pageable);
        } else {
            pagina = repositorio.findByProyectoId(proyectoId, pageable);
        }
        return pagina.map(TareaResponse::desde);
    }

    @Transactional(readOnly = true)
    public TareaResponse buscarPorId(Long id) {
        return TareaResponse.desde(buscarEntidad(id));
    }

    @Transactional
    public TareaResponse crear(Long proyectoId, TareaRequest request) {
        Proyecto proyecto = proyectoServicio.buscarEntidad(proyectoId);
        Prioridad prioridad = request.prioridad() != null ? request.prioridad() : Prioridad.MEDIA;
        Tarea tarea = new Tarea(
                request.titulo(), request.descripcion(),
                prioridad, request.fechaLimite(), proyecto);
        return TareaResponse.desde(repositorio.save(tarea));
    }

    @Transactional
    public TareaResponse actualizar(Long id, TareaRequest request) {
        Tarea tarea = buscarEntidad(id);
        tarea.setTitulo(request.titulo());
        tarea.setDescripcion(request.descripcion());
        if (request.prioridad() != null) {
            tarea.setPrioridad(request.prioridad());
        }
        tarea.setFechaLimite(request.fechaLimite());
        return TareaResponse.desde(repositorio.save(tarea));
    }

    @Transactional
    public TareaResponse cambiarEstado(Long id, EstadoTarea nuevoEstado) {
        Tarea tarea = buscarEntidad(id);
        tarea.setEstado(nuevoEstado);
        return TareaResponse.desde(repositorio.save(tarea));
    }

    @Transactional
    public void eliminar(Long id) {
        if (!repositorio.existsById(id)) {
            throw new RecursoNoEncontradoException("Tarea no encontrada con id: " + id);
        }
        repositorio.deleteById(id);
    }

    private Tarea buscarEntidad(Long id) {
        return repositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Tarea no encontrada con id: " + id));
    }
}
