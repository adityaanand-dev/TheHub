package com.thehub.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class ServiceRequest {

    @JsonProperty("creator_name")
    @JsonAlias({"creatorName", "creator_name"})
    private String creatorName;

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Category is required")
    private String category;

    @NotNull(message = "Rate is required")
    @DecimalMin(value = "0.01", message = "Rate must be greater than 0")
    private BigDecimal rate;

    @NotBlank(message = "Description is required")
    private String description;

    @JsonProperty("delivery_days")
    @JsonAlias({"deliveryDays", "delivery_days"})
    private Integer deliveryDays;

    public ServiceRequest() {}

    public ServiceRequest(String creatorName, String title, String category, BigDecimal rate, String description, Integer deliveryDays) {
        this.creatorName = creatorName;
        this.title = title;
        this.category = category;
        this.rate = rate;
        this.description = description;
        this.deliveryDays = deliveryDays != null ? deliveryDays : 3;
    }

    public String getCreatorName() { return creatorName; }
    public void setCreatorName(String creatorName) { this.creatorName = creatorName; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public BigDecimal getRate() { return rate; }
    public void setRate(BigDecimal rate) { this.rate = rate; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getDeliveryDays() { return deliveryDays; }
    public void setDeliveryDays(Integer deliveryDays) { this.deliveryDays = deliveryDays; }
}
