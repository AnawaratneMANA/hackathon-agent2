package com.hackathon.hackathon.agent2.dto;

public class ItemListDTO {
    private String itemId;
    private String itemName;
    private Integer currentStock;
    private Double dailyConsumption;
    private Integer leadTimeDays;
    private Double daysOfCover;
    private String vendorType;

    public ItemListDTO(
            String itemId,
            String itemName,
            Integer currentStock,
            Double dailyConsumption,
            Integer leadTimeDays,
            Double daysOfCover,
            String vendorType
    ) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.currentStock = currentStock;
        this.dailyConsumption = dailyConsumption;
        this.leadTimeDays = leadTimeDays;
        this.daysOfCover = daysOfCover;
        this.vendorType = vendorType;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public Integer getCurrentStock() {
        return currentStock;
    }

    public void setCurrentStock(Integer currentStock) {
        this.currentStock = currentStock;
    }

    public Double getDailyConsumption() {
        return dailyConsumption;
    }

    public void setDailyConsumption(Double dailyConsumption) {
        this.dailyConsumption = dailyConsumption;
    }

    public Integer getLeadTimeDays() {
        return leadTimeDays;
    }

    public void setLeadTimeDays(Integer leadTimeDays) {
        this.leadTimeDays = leadTimeDays;
    }

    public Double getDaysOfCover() {
        return daysOfCover;
    }

    public void setDaysOfCover(Double daysOfCover) {
        this.daysOfCover = daysOfCover;
    }

    public String getVendorType() {
        return vendorType;
    }

    public void setVendorType(String vendorType) {
        this.vendorType = vendorType;
    }
}

