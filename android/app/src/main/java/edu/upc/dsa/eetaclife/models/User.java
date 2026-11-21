package edu.upc.dsa.eetaclife.models;

import java.util.List;

public class User {
    private String idUser;
    private String username;
    private String email;
    private String password;
    private int coins;
    private int energy;
    private int ects;
    private boolean graduated;
    private List<String> passedMinigames;

    public User() {}

    public String getIdUser() { return idUser; }
    public void setIdUser(String idUser) { this.idUser = idUser; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public int getCoins() { return coins; }
    public void setCoins(int coins) { this.coins = coins; }

    public int getEnergy() { return energy; }
    public void setEnergy(int energy) { this.energy = energy; }

    public int getEcts() { return ects; }
    public void setEcts(int ects) { this.ects = ects; }

    public boolean isGraduated() { return graduated; }
    public void setGraduated(boolean graduated) { this.graduated = graduated; }

    public List<String> getPassedMinigames() { return passedMinigames; }
    public void setPassedMinigames(List<String> passedMinigames) { this.passedMinigames = passedMinigames; }
}