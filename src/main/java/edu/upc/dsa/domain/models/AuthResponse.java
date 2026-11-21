package edu.upc.dsa.domain.models;

/**
 * Data Transfer Object (DTO) returned upon successful student login.
 * Encapsulates the unique session Bearer token along with the authenticated User profile.
 *
 * No posem el token com un atribut dins de User perquè l'usuari és una entitat permanent del sistema,
 * el token és una clau temporal de sessió.
 * A més, d'aquesta manera un mateix estudiant pot tenir múltiples sessions obertes alhora (per exemple, al mòbil i al navegador web)
 * amb tokens diferents apuntant al mateix User sense sobreescriure's.
 * AuthResponse simplement empaqueta el token generat i l'objecte User en una sola resposta al moment de fer login,
 * mentre que la resta d'endpoints (com /me o les compres a cafeteria) retornen únicament l'objecte User actualitzat.
 */
public class AuthResponse {
    private String token;
    private User user;

    // Empty constructor required by Jersey/Jackson for JSON serialization
    public AuthResponse() {}

    public AuthResponse(String token, User user) {
        this.token = token;
        this.user = user;
    }

    // Getters and Setters
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}