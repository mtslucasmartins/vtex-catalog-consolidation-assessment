package com.vtex.catalog.worker.application.common.helpers;

import java.text.Normalizer;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

public final class StringHelper {

    private static final Pattern STRICT_UUID =
            Pattern.compile("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$", Pattern.CASE_INSENSITIVE);

    private StringHelper() {
    }

    public static String normalize(String value) {
        return value.trim().replaceAll("\\s+", " ");
    }

    public static String canonical(String value) {
        if (value == null) {
            return "";
        }
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", "-")
                .replaceAll("[^a-z0-9-]", "")
                .replaceAll("-+", "-")
                .replaceAll("^-+|-+$", "");
    }

    public static String sku(String brand, String name) {
        return canonical(brand) + "#" + canonical(name);
    }

    public static boolean isUuid(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        var trimmed = value.trim();
        if (!STRICT_UUID.matcher(trimmed).matches()) {
            return false;
        }
        try {
            UUID.fromString(trimmed);
            return true;
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }
}
