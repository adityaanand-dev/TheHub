package com.thehub.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.math.BigDecimal;

public class StatsResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("total_gigs")
    private Long totalGigs;

    @JsonProperty("total_bookings")
    private Long totalBookings;

    @JsonProperty("pending_bookings")
    private Long pendingBookings;

    @JsonProperty("accepted_bookings")
    private Long acceptedBookings;

    @JsonProperty("total_volume")
    private BigDecimal totalVolume;

    @JsonProperty("total_users")
    private Long totalUsers;

    public StatsResponse() {}

    public StatsResponse(Long totalGigs, Long totalBookings, Long pendingBookings, Long acceptedBookings, BigDecimal totalVolume, Long totalUsers) {
        this.totalGigs = totalGigs;
        this.totalBookings = totalBookings;
        this.pendingBookings = pendingBookings;
        this.acceptedBookings = acceptedBookings;
        this.totalVolume = totalVolume;
        this.totalUsers = totalUsers;
    }

    public Long getTotalGigs() { return totalGigs; }
    public void setTotalGigs(Long totalGigs) { this.totalGigs = totalGigs; }

    public Long getTotalBookings() { return totalBookings; }
    public void setTotalBookings(Long totalBookings) { this.totalBookings = totalBookings; }

    public Long getPendingBookings() { return pendingBookings; }
    public void setPendingBookings(Long pendingBookings) { this.pendingBookings = pendingBookings; }

    public Long getAcceptedBookings() { return acceptedBookings; }
    public void setAcceptedBookings(Long acceptedBookings) { this.acceptedBookings = acceptedBookings; }

    public BigDecimal getTotalVolume() { return totalVolume; }
    public void setTotalVolume(BigDecimal totalVolume) { this.totalVolume = totalVolume; }

    public Long getTotalUsers() { return totalUsers; }
    public void setTotalUsers(Long totalUsers) { this.totalUsers = totalUsers; }
}
