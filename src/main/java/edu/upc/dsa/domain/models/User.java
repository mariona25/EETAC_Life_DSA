package edu.upc.dsa.domain.models;

import edu.upc.dsa.util.RandomUtils;
import java.util.ArrayList;
import java.util.List;

public class User {
    private String idUser;
    private String username;
    private String email;
    private String password;
    private int ects;
    private int energy;
    private int coins;
    private boolean isGraduated;
    private List<String> passedMinigames;

    // 1. Empty constructor (required by Jersey/Jackson for JSON serialization; generates an automatic ID and default stats)
    public User() {
        this.idUser = RandomUtils.getId();
        this.ects = 0;
        this.energy = 100; //PENDENT A CANVIAR SEGONS ELS VALORS QUE DECIDIM
        this.coins = 20; //PENDENT A CANVIAR SEGONS ELS VALORS QUE DECIDIM
        this.isGraduated = false;
        this.passedMinigames = new ArrayList<>();
    }

    // 2. Constructor for creating a new user (includes username, email, and password)
    public User(String username, String email, String password) {
        this(); // Calls the empty constructor to generate the ID and set 0 ECTS, 100 Energy, 20 Coins
        this.username = username;
        this.email = email;
        this.password = password;
    }

    // Getters and Setters
    public String getIdUser() { return idUser; }
    public void setIdUser(String idUser) { this.idUser = idUser; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public int getEcts() { return ects; }
    public void setEcts(int ects) { this.ects = ects; }

    public int getEnergy() { return energy; }
    public void setEnergy(int energy) { this.energy = energy; }

    public int getCoins() { return coins; }
    public void setCoins(int coins) { this.coins = coins; }

    public boolean isGraduated() { return isGraduated; }
    public void setGraduated(boolean graduated) { isGraduated = graduated; }

    public List<String> getPassedMinigames() { return passedMinigames; }
    public void setPassedMinigames(List<String> passedMinigames) { this.passedMinigames = passedMinigames; }

    @Override
    public String toString() {
        return "User [idUser=" + idUser + ", username=" + username + ", email=" + email + ", ects=" + ects + ", energy=" + energy + ", coins=" + coins + "]";
    }
}