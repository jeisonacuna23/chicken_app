package com.pollosk.model;

public class ItemVenta {
    public long id;
    public long ventaId;
    public long polloId;
    public double pesoKg;
    public double subtotal;

    public ItemVenta() {}

    public ItemVenta(long ventaId, long polloId, double pesoKg, double subtotal) {
        this.ventaId = ventaId;
        this.polloId = polloId;
        this.pesoKg = pesoKg;
        this.subtotal = subtotal;
    }
}
