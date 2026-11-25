package edu.upc.dsa.eetaclife.models;

import java.util.List;

public class User {

    private String idUser;
    private String username;
    private String email;
    private int coins;
    private int energy;
    private int ects;
    private boolean graduated;
    private List<String> passedMinigames;

    public User() {}

    public String getIdUser() { return idUser; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public int getCoins() { return coins; }
    public int getEnergy() { return energy; }
    public int getEcts() { return ects; }
    public boolean isGraduated() { return graduated; }
    public List<String> getPassedMinigames() { return passedMinigames; }
}