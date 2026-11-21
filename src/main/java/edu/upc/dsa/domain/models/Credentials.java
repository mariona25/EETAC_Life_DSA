package edu.upc.dsa.domain.models;

/**
 * Data Transfer Object (DTO) used to receive student authentication data from client JSON requests.
 * Reused for both registration (username, email, password) and login (username, password).

 * En lloc de rebre un objecte User sencer al registre o al login,
 * fem servir aquesta classe perquè el client (Android o Web) només enviï els camps estrictament necessaris (username, email i password).
 * Això simplifica les peticions des del frontend, deixa la documentació de Swagger molt més clara
 * i evita problemes de seguretat (com que algú pugui modificar o injectar valors inicials de monedes o ECTS en registrar-se).
 */
public class Credentials {
    private String username;
    private String email;
    private String password;

    // Empty constructor required by Jersey/Jackson for JSON deserialization
    public Credentials() {}

    public Credentials(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }

    // Getters and Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}