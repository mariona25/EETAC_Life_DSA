package edu.upc.dsa.domain.models;

public class CafeteriaOrder {
    private String userId;
    private String producteId;

    // Constructor buit obligatori per a Jersey/JSON
    public CafeteriaOrder() {}

    public CafeteriaOrder(String userId, String producteId) {
        this.userId = userId;
        this.producteId = producteId;
    }

    // Getters i Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getProducteId() { return producteId; }
    public void setProducteId(String producteId) { this.producteId = producteId; }

    @Override
    public String toString() {
        return "CafeteriaOrder [userId=" + userId + ", producteId=" + producteId + "]";
    }
}