package com.vtex.catalog.worker.application.common.stereotypes;

import com.vtex.catalog.worker.application.common.exception.DomainException;
import com.vtex.catalog.worker.application.common.helpers.StringHelper;

public abstract class AssertionConcern {

    public void assertNotEmpty(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new DomainException(message);
        }
    }

    public void assertContainsLetterOrNumber(String value, String message) {
        if (StringHelper.canonical(value).isEmpty()) {
            throw new DomainException(message);
        }
    }
}
