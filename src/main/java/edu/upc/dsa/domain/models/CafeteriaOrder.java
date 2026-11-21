package edu.upc.dsa.domain.models;

/**
 * Data Transfer Object (DTO) representing a cafeteria purchase order.
 * Receives the producteId from the client JSON request and gets populated with the authenticated userId on the server.
 */
public class CafeteriaOrder {
    private String userId;
    private String producteId;

    // Empty constructor required by Jersey/Jackson for JSON serialization
    public CafeteriaOrder() {}

    public CafeteriaOrder(String userId, String producteId) {
        this.userId = userId;
        this.producteId = producteId;
    }

    // Getters and Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getProducteId() { return producteId; }
    public void setProducteId(String producteId) { this.producteId = producteId; }

    @Override
    public String toString() {
        return "CafeteriaOrder [userId=" + userId + ", producteId=" + producteId + "]";
    }
}