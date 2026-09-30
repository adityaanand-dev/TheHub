package com.thehub.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ServiceResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    @JsonProperty("creator_name")
    private String creatorName;

    private String title;
    private String category;
    private BigDecimal rate;
    private String description;

    @JsonProperty("delivery_days")
    private Integer deliveryDays;

    private Boolean active;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("freelancer_id")
    private Long freelancerId;

    @JsonProperty("rating_avg")
    private Double ratingAvg;

    @JsonProperty("rating_count")
    private Integer ratingCount;

    public ServiceResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Long getFreelancerId() { return freelancerId; }
    public void setFreelancerId(Long freelancerId) { this.freelancerId = freelancerId; }

    public Double getRatingAvg() { return ratingAvg; }
    public void setRatingAvg(Double ratingAvg) { this.ratingAvg = ratingAvg; }

    public Integer getRatingCount() { return ratingCount; }
    public void setRatingCount(Integer ratingCount) { this.ratingCount = ratingCount; }
}
