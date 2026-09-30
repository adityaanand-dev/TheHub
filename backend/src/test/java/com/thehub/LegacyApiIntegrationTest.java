package com.thehub;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thehub.dto.OrderRequest;
import com.thehub.dto.OrderStatusUpdateRequest;
import com.thehub.dto.ServiceRequest;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class LegacyApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static Long createdGigId;
    private static Long bookingId1;
    private static Long bookingId2;

    @Test
    @Order(1)
    void test1_RootHealthCheck() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", containsString("Marketplace API operational")));
    }

    @Test
    @Order(2)
    void test2_AutoSeededGigsCheck() throws Exception {
        mockMvc.perform(get("/api/gigs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(6))));
    }

    @Test
    @Order(3)
    void test3_PostGig() throws Exception {
        ServiceRequest newGig = new ServiceRequest(
                "Jordan Smith",
                "3D Blender Product Animations",
                "Design & Graphics",
                BigDecimal.valueOf(110.0),
                "High resolution 3D renders and rotating showcase clips for eCommerce.",
                3
        );

        String response = mockMvc.perform(post("/api/gigs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newGig)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.message", containsString("Gig posted successfully")))
                .andReturn().getResponse().getContentAsString();

        createdGigId = objectMapper.readTree(response).get("id").asLong();
    }

    @Test
    @Order(4)
    void test4_BrowseSearchAndDP3Sort() throws Exception {
        // Search by keyword
        mockMvc.perform(get("/api/gigs").param("search", "Blender"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].title", containsString("Blender")));

        // Filter by category
        mockMvc.perform(get("/api/gigs").param("category", "Video & UGC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].category", is("Video & UGC")));

        // DP3 Sort Cheapest
        mockMvc.perform(get("/api/gigs").param("sort_by", "cheapest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].rate", notNullValue()));

        // DP3 Sort Priciest
        mockMvc.perform(get("/api/gigs").param("sort_by", "priciest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].rate", notNullValue()));
    }

    @Test
    @Order(5)
    void test5_GigValidation404OnInvalidGig() throws Exception {
        OrderRequest badBooking = new OrderRequest(999999L, "Test Client", "test@example.com", "Need animation");
        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badBooking)))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(6)
    void test6_BookGigAndDP2ConcurrentBooking() throws Exception {
        Long targetGigId = (createdGigId != null) ? createdGigId : 1L;

        OrderRequest booking1 = new OrderRequest(targetGigId, "Acme Brand", "brand@acme.com", "Need 15-second 3D spin");
        String res1 = mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(booking1)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is("Pending")))
                .andReturn().getResponse().getContentAsString();

        bookingId1 = objectMapper.readTree(res1).get("id").asLong();

        // DP2: Submit 2nd booking while 1st is Pending
        OrderRequest booking2 = new OrderRequest(targetGigId, "Nova Tech", "marketing@novatech.io", "Need hero render");
        String res2 = mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(booking2)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is("Pending")))
                .andReturn().getResponse().getContentAsString();

        bookingId2 = objectMapper.readTree(res2).get("id").asLong();
    }

    @Test
    @Order(7)
    void test7_CreatorDashboardAndStatusDecisions() throws Exception {
        // Creator views inquiries
        mockMvc.perform(get("/api/creator/bookings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))));

        // Accept booking 1
        OrderStatusUpdateRequest acceptReq = new OrderStatusUpdateRequest("Accepted", null);
        mockMvc.perform(patch("/api/bookings/" + bookingId1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(acceptReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("Accepted")));

        // Attempting to decline accepted booking must throw 409
        OrderStatusUpdateRequest conflictReq = new OrderStatusUpdateRequest("Declined", "Too late");
        mockMvc.perform(patch("/api/bookings/" + bookingId1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(conflictReq)))
                .andExpect(status().isConflict());

        // Decline booking 2 with DP1 feedback
        OrderStatusUpdateRequest declineReq = new OrderStatusUpdateRequest("Declined", "Currently booked for 2 weeks. Try our Design partners!");
        mockMvc.perform(patch("/api/bookings/" + bookingId2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(declineReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("Declined")));
    }

    @Test
    @Order(8)
    void test8_ClientBookingsView() throws Exception {
        mockMvc.perform(get("/api/client/bookings").param("client_name", "Acme Brand"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status", is("Accepted")));

        mockMvc.perform(get("/api/client/bookings").param("client_email", "brand@acme.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].client_email", is("brand@acme.com")));

        mockMvc.perform(get("/api/client/bookings").param("client_name", "Nova Tech"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status", is("Declined")))
                .andExpect(jsonPath("$[0].rejection_reason", containsString("Currently booked")));
    }

    @Test
    @Order(9)
    void test9_PlatformLiveStats() throws Exception {
        mockMvc.perform(get("/api/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total_gigs", greaterThanOrEqualTo(6)))
                .andExpect(jsonPath("$.total_bookings", greaterThanOrEqualTo(2)))
                .andExpect(jsonPath("$.accepted_bookings", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.total_volume", greaterThanOrEqualTo(0.0)));
    }
}
