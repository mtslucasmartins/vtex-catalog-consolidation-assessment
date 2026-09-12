package com.vtex.catalog.ingester.application.common.helpers;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StringHelperTest {

    @Test
    void givenExtraSpaces_whenNormalize_thenTrimsAndCollapses() {
        // When
        var normalized = StringHelper.normalize("  Mega   Store  ");

        // Then
        assertEquals("Mega Store", normalized);
    }

    @Test
    void givenMixedCaseAndSpaces_whenCanonical_thenSlugifies() {
        // When
        var canonical = StringHelper.canonical("Smartphone Galaxy S23");

        // Then
        assertEquals("smartphone-galaxy-s23", canonical);
    }

    @Test
    void givenBrandAndName_whenSku_thenBuildsCanonicalSku() {
        // When
        var sku = StringHelper.sku("Samsung", "Smartphone Galaxy S23");

        // Then
        assertEquals("samsung#smartphone-galaxy-s23", sku);
    }
}
