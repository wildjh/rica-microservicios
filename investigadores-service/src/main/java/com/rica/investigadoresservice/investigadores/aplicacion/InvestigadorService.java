package com.rica.investigadoresservice.investigadores.aplicacion;

import java.util.List;

import com.rica.investigadoresservice.investigadores.dominio.Investigador;
import org.springframework.stereotype.Service;

import com.rica.investigadoresservice.compartido.CorreoDuplicadoException;
import com.rica.investigadoresservice.compartido.RecursoNoEncontradoException;

@Service
public class InvestigadorService implements InvestigadorUseCase {
    private final RepositorioInvestigadores repositorioInvestigadores;
    private final InvestigadorFactory investigadorFactory;

    public InvestigadorService(RepositorioInvestigadores repositorioInvestigadores, InvestigadorFactory investigadorFactory) {
        this.repositorioInvestigadores = repositorioInvestigadores;
        this.investigadorFactory = investigadorFactory;
    }

    public List<Investigador> listarTodos() {
        return repositorioInvestigadores.listarTodos();
    }

    public Investigador buscarPorId(Long id) {
        return repositorioInvestigadores.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe un investigador con id " + id));
    }

    public Investigador registrar(String nombre, String correoInstitucional, String grupoInvestigacion) {
        Investigador investigador = investigadorFactory.crear(nombre, correoInstitucional, grupoInvestigacion);
        if (repositorioInvestigadores.existeCorreo(investigador.getCorreoInstitucional().valor())) {
            throw new CorreoDuplicadoException(
                    "Ya existe un investigador registrado con el correo " + investigador.getCorreoInstitucional());
        }
        return repositorioInvestigadores.guardar(investigador);
    }
}