package com.rica.publicaciones_service.compartido;

public class CorreoDuplicadoException extends RuntimeException{

    public CorreoDuplicadoException(String mensaje){
        super(mensaje);
    }
}
