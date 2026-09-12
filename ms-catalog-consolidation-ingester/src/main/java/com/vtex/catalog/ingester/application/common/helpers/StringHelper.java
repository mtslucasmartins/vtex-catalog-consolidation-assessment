package com.vtex.catalog.ingester.application.common.helpers;

import java.text.Normalizer;
import java.util.Locale;

public final class StringHelper {

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
}
