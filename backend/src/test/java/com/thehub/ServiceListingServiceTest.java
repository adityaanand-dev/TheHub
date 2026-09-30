package com.thehub.service;

import com.thehub.dto.ServiceRequest;
import com.thehub.dto.ServiceResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ServiceListingServiceTest {

    @Autowired
    private ServiceListingService serviceListingService;

    @Test
    void testSearchAndSortGigs_DP3() {
        // Post a custom gig
        ServiceRequest newGig = new ServiceRequest(
                "Jordan Smith",
                "3D Blender Product Animations",
                "Design & Graphics",
                BigDecimal.valueOf(110.0),
                "High resolution 3D renders and rotating showcase clips for eCommerce.",
                3
        );
        ServiceResponse created = serviceListingService.createService(newGig, null);
        assertNotNull(created.getId());

        // Search by keyword
        List<ServiceResponse> searchResults = serviceListingService.searchServices(null, "Blender", null, null, null, "newest");
        assertFalse(searchResults.isEmpty());
        assertTrue(searchResults.get(0).getTitle().contains("Blender"));

        // Sort by cheapest
        List<ServiceResponse> cheapest = serviceListingService.searchServices(null, null, null, null, null, "cheapest");
        for (int i = 0; i < cheapest.size() - 1; i++) {
            assertTrue(cheapest.get(i).getRate().compareTo(cheapest.get(i + 1).getRate()) <= 0);
        }

        // Sort by priciest
        List<ServiceResponse> priciest = serviceListingService.searchServices(null, null, null, null, null, "priciest");
        for (int i = 0; i < priciest.size() - 1; i++) {
            assertTrue(priciest.get(i).getRate().compareTo(priciest.get(i + 1).getRate()) >= 0);
        }
    }
}
