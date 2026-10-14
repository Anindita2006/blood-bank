package com.bloodbank;

// One row of data: "Bank X in City Y has N units of blood group Z".
// A normal Java class (POJO): private fields + constructors + getters/setters.
// Spring converts it to JSON automatically using the getters.
public class Stock {

    private String bank;
    private String city;
    private String group;
    private int units;

    // Empty constructor: Spring needs it to build a Stock from incoming JSON
    public Stock() {}

    public Stock(String bank, String city, String group, int units) {
        this.bank = bank;
        this.city = city;
        this.group = group;
        this.units = units;
    }

    // Fewer than 5 units = running low. Because it's named isLow(),
    // Spring adds "low": true/false to the JSON automatically.
    public boolean isLow() { return units < 5; }

    public String getBank()  { return bank; }
    public String getCity()  { return city; }
    public String getGroup() { return group; }
    public int getUnits()    { return units; }

    public void setBank(String bank)   { this.bank = bank; }
    public void setCity(String city)   { this.city = city; }
    public void setGroup(String group) { this.group = group; }
    public void setUnits(int units)    { this.units = units; }
}
