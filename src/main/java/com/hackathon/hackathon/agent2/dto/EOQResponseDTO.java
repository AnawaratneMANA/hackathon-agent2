package com.hackathon.hackathon.agent2.dto;

public class EOQResponseDTO {
    private String itemId;
    private double annualDemand;
    private double orderingCost;
    private double holdingCostPerUnit;
    private int eoq;

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public double getAnnualDemand() {
        return annualDemand;
    }

    public void setAnnualDemand(double annualDemand) {
        this.annualDemand = annualDemand;
    }

    public double getOrderingCost() {
        return orderingCost;
    }

    public void setOrderingCost(double orderingCost) {
        this.orderingCost = orderingCost;
    }

    public double getHoldingCostPerUnit() {
        return holdingCostPerUnit;
    }

    public void setHoldingCostPerUnit(double holdingCostPerUnit) {
        this.holdingCostPerUnit = holdingCostPerUnit;
    }

    public int getEoq() {
        return eoq;
    }

    public void setEoq(int eoq) {
        this.eoq = eoq;
    }
}