package edu.upc.dsa.domain.models;

import edu.upc.dsa.util.RandomUtils;

/**
 * Domain model representing a food or drink item available in the EETAC cafeteria.
 */
public class CafeteriaItem {
    private String idItem;
    private String name;
    private int priceCoins;
    private int energyRecovery;

    // 1. Empty constructor (required by Jersey/Jackson for JSON serialization; generates an automatic ID)
    public CafeteriaItem() {
        this.idItem = RandomUtils.getId();
    }

    // 2. Full constructor (useful for initializing catalog items with fixed IDs like "caf_1")
    public CafeteriaItem(String idItem, String name, int priceCoins, int energyRecovery) {
        this.idItem = idItem;
        this.name = name;
        this.priceCoins = priceCoins;
        this.energyRecovery = energyRecovery;
    }

    // 3. Constructor without ID (generates an automatic ID via the empty constructor)
    public CafeteriaItem(String name, int priceCoins, int energyRecovery) {
        this();
        this.name = name;
        this.priceCoins = priceCoins;
        this.energyRecovery = energyRecovery;
    }

    // Getters and Setters
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