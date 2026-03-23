package com.example.recupera_plus;

public class Cita {

    private String nombreUsuario;
    private String especialista;
    private String descripcion;
    private String fechaReserva;
    private String estado;

    // Constructor vacío (requerido por Firebase)
    public Cita() {}

    public Cita(String nombreUsuario, String especialista, String descripcion, String fechaReserva, String estado) {
        this.nombreUsuario = nombreUsuario;
        this.especialista = especialista;
        this.descripcion = descripcion;
        this.fechaReserva = fechaReserva;
        this.estado = estado;
    }

    // Getters y Setters
    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public String getEspecialista() {
        return especialista;
    }

    public void setEspecialista(String especialista) {
        this.especialista = especialista;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getFechaReserva() {
        return fechaReserva;
    }

    public void setFechaReserva(String fechaReserva) {
        this.fechaReserva = fechaReserva;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
