package com.vtex.catalog.worker.application.common.helpers;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StringHelperTest {

    @Test
    void givenExtraSpaces_whenNormalize_thenTrimsAndCollapses() {
        // Given
        var raw = "  Mega   Store  ";

        // When
        var normalized = StringHelper.normalize(raw);

        // Then
        assertEquals("Mega Store", normalized);
    }

    @Test
    void givenMixedCaseAndSpaces_whenCanonical_thenSlugifies() {
        // Given
        var raw = "Smartphone Galaxy S23";

        // When
        var canonical = StringHelper.canonical(raw);

        // Then
        assertEquals("smartphone-galaxy-s23", canonical);
    }

    @Test
    void givenBrandAndName_whenSku_thenBuildsCanonicalSku() {
        // Given
        var brand = "Samsung";
        var name = "Smartphone Galaxy S23";

        // When
        var sku = StringHelper.sku(brand, name);

        // Then
        assertEquals("samsung#smartphone-galaxy-s23", sku);
    }

    @Test
    void givenValidUuid_whenIsUuid_thenReturnsTrue() {
        // Given
        var value = "a1b2c3d4-e5f6-4a5b-8c9d-0e1f2a3b4c5d";

        // When / Then
        assertTrue(StringHelper.isUuid(value));
    }

    @Test
    void givenInvalidUuid_whenIsUuid_thenReturnsFalse() {
        // Given
        var value = "not-a-uuid";

        // When / Then
        assertFalse(StringHelper.isUuid(value));
        assertFalse(StringHelper.isUuid(null));
        assertFalse(StringHelper.isUuid(" "));
    }

    @Test
    void givenSevenCharFirstSegment_whenIsUuid_thenReturnsFalse() {
        // Java UUID.fromString zero-pads short groups; strict validation must reject this.
        assertFalse(StringHelper.isUuid("ddddeee-ffff-4000-1111-222233334444"));
    }
}
