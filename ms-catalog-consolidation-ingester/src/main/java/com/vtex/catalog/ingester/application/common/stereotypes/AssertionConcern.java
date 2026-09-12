package com.vtex.catalog.ingester.application.common.stereotypes;

import com.vtex.catalog.ingester.application.common.exception.DomainException;
import com.vtex.catalog.ingester.application.common.helpers.StringHelper;

public abstract class AssertionConcern {

    protected void assertNotEmpty(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new DomainException(message);
        }
    }

    protected void assertContainsLetterOrNumber(String value, String message) {
        if (StringHelper.canonical(value).isEmpty()) {
            throw new DomainException(message);
        }
    }
}
