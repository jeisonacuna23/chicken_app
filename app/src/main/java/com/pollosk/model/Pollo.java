package com.pollosk.model;

public class Pollo {
    public long id;
    public double pesoKg;
    public boolean vendido;
    public Long ventaId; // null si no se ha vendido

    public Pollo() {}

    public Pollo(long id, double pesoKg, boolean vendido, Long ventaId) {
        this.id = id;
        this.pesoKg = pesoKg;
        this.vendido = vendido;
        this.ventaId = ventaId;
    }
}
