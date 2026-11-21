package edu.upc.dsa.eetaclife.models;

public class User {
    private String idUser;
    private String username;
    private String password;
    private int ects;
    private int energy;
    private int coins;
    private boolean isGraduated;

    public User() {}

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getIdUser() { return idUser; }
    public void setIdUser(String idUser) { this.idUser = idUser; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

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
}