package edu.upc.dsa.domain.models;

import edu.upc.dsa.util.RandomUtils;

public class CafeteriaItem {
    private String idItem;
    private String name;
    private int priceCoins;
    private int energyRecovery;

    // 1. Constructor buit (obligatori per a Jersey/JSON i genera ID automàtic si cal)
    public CafeteriaItem() {
        this.idItem = RandomUtils.getId();
    }

    // 2. Constructor amb tots els camps (útil per crear productes amb un ID fix com "cafe_espresso")
    public CafeteriaItem(String idItem, String name, int priceCoins, int energyRecovery) {
        this.idItem = idItem;
        this.name = name;
        this.priceCoins = priceCoins;
        this.energyRecovery = energyRecovery;
    }

    // 3. Constructor sense ID (per si es crea un producte nou i volem ID automàtic)
    public CafeteriaItem(String name, int priceCoins, int energyRecovery) {
        this();
        this.name = name;
        this.priceCoins = priceCoins;
        this.energyRecovery = energyRecovery;
    }

    // Getters i Setters
    public String getIdItem() { return idItem; }
    public void setIdItem(String idItem) { this.idItem = idItem; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getPriceCoins() { return priceCoins; }
    public void setPriceCoins(int priceCoins) { this.priceCoins = priceCoins; }

    public int getEnergyRecovery() { return energyRecovery; }
    public void setEnergyRecovery(int energyRecovery) { this.energyRecovery = energyRecovery; }

    @Override
    public String toString() {
        return "CafeteriaItem [idItem=" + idItem + ", name=" + name + ", priceCoins=" + priceCoins + ", energyRecovery=" + energyRecovery + "]";
    }
}