package com.pollosk.model;

public class Venta {
    public static final String METODO_EFECTIVO = "Efectivo";
    public static final String METODO_NEQUI    = "Nequi";
    public static final String METODO_FIADO    = "Fiado";

    public long id;
    public String referencia;
    public String cliente;
    public double precioKg;
    public String metodoPago;
    public String fecha; // ISO 8601
    public double total;

    public Venta() {}
}
