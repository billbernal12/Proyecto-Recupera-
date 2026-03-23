package com.example.recupera_plus;

public class Usuario {
    // Paso 1
    public String tipoDoc;
    public String numDoc;
    public String fechaNac;
    public boolean aceptaTerminos;
    public boolean aceptaPromociones;

    // Paso 2
    public String nombres;
    public String apellidos;
    public String celular;
    public String genero;
    public int peso;
    public int talla;

    // Paso 3
    public String correo;
    public String contrasena;

    // Constructor vacío (requerido por Firebase)
    public Usuario() {}

    // Constructor completo
    public Usuario(String tipoDoc, String numDoc, String fechaNac,
                   boolean aceptaTerminos, boolean aceptaPromociones,
                   String nombres, String apellidos, String celular,
                   String genero, int peso, int talla,
                   String correo, String contrasena) {
        this.tipoDoc = tipoDoc;
        this.numDoc = numDoc;
        this.fechaNac = fechaNac;
        this.aceptaTerminos = aceptaTerminos;
        this.aceptaPromociones = aceptaPromociones;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.celular = celular;
        this.genero = genero;
        this.peso = peso;
        this.talla = talla;
        this.correo = correo;
        this.contrasena = contrasena;
    }
}
