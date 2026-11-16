package com.rica.investigadoresservice.compartido;

public class CorreoDuplicadoException extends RuntimeException{

    public CorreoDuplicadoException(String mensaje){
        super(mensaje);
    }
}
