package org.example.work1;

public class MutualFund2 {
    private String schemeCode;
    private String schemeName;
    private double nav;
    private double amountInvested;
    private double costPerUnit;
    private double currentValue;
    private double units;

    public MutualFund2(String schemeCode, String schemeName, double nav, double amountInvested, double currentValue, double units, double costPerUnit) {
        this.schemeCode = schemeCode;
        this.schemeName = schemeName;
        this.nav = nav;
        this.amountInvested = amountInvested;
        this.currentValue = currentValue;
        this.units = units;
        this.costPerUnit = costPerUnit;

        // Avoid division by zero

    }

    // Getters and setters for all properties

    public String getSchemeCode() {
        return schemeCode;
    }

    public void setSchemeCode(String schemeCode) {
        this.schemeCode = schemeCode;
    }

    public String getSchemeName() {
        return schemeName;
    }

    public void setSchemeName(String schemeName) {
        this.schemeName = schemeName;
    }

    public double getNav() {
        return nav;
    }

    public void setNav(double nav) {
        this.nav = nav;
    }

    public double getAmountInvested() {
        return amountInvested;
    }

    public void setAmountInvested(double amountInvested) {
        this.amountInvested = amountInvested;
    }

    public double getCostPerUnit() {
        return costPerUnit;
    }

    public void setCostPerUnit(double costPerUnit) {
        this.costPerUnit = costPerUnit;
    }

    public double getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(double currentValue) {
        this.currentValue = currentValue;
    }

    public double getUnits() {
        return units;
    }

    public void setUnits(double units) {
        this.units = units;
    }
}
