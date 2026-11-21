package edu.upc.dsa.eetaclife.models;

public class CafeteriaOrder {
    private String producteId;

    public CafeteriaOrder(String producteId) {
        this.producteId = producteId;
    }

    public String getProducteId() { return producteId; }
}