package com.rica.publicaciones_service.publicaciones;

import com.rica.publicaciones_service.compartido.RecursoNoEncontradoException;
import com.rica.publicaciones_service.publicaciones.VerificadorInvestigador;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PublicacionService {

    private final PublicacionRepository publicacionRepository;
    private final VerificadorInvestigador verificadorInvestigador; // antes: InvestigadorRepository
    private final LimitePublicacionesAnualesService limitePublicacionesAnualesService;

    public PublicacionService(PublicacionRepository publicacionRepository,
                              VerificadorInvestigador investigadorRepository,
                              LimitePublicacionesAnualesService limitePublicacionesAnualesService) {
        this.publicacionRepository = publicacionRepository;
        this.verificadorInvestigador = investigadorRepository;
        this.limitePublicacionesAnualesService = limitePublicacionesAnualesService;
    }

    public Publicacion registrar(Publicacion publicacion) {
        if (!verificadorInvestigador.existe(publicacion.getInvestigadorCorreo())) {
            throw new RecursoNoEncontradoException(
                    "No existe un investigador con correo " + publicacion.getInvestigadorCorreo());
        }
        if (!limitePublicacionesAnualesService.puedeRegistrar(publicacion.getInvestigadorCorreo(), publicacion.getAnio())) {
            throw new LimiteAnualExcedidoException(
                    "El investigador " + publicacion.getInvestigadorCorreo()
                            + " ya alcanzó el máximo de publicaciones registradas para " + publicacion.getAnio());
        }
        return publicacionRepository.save(publicacion);
    }

    public List<Publicacion> listarPorInvestigador(String investigadorCorreo) {
        return publicacionRepository.findByInvestigadorCorreo(investigadorCorreo);
    }

    public Publicacion buscarPorId(String id) {
        return publicacionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe una publicación con id " + id));
    }

}