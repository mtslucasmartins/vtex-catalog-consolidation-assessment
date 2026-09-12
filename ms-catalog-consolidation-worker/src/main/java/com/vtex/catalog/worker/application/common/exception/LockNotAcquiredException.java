package com.vtex.catalog.worker.application.common.exception;

public class LockNotAcquiredException extends DomainException {

    public LockNotAcquiredException(String resourceKey) {
        super("Could not acquire lock for resource: " + resourceKey);
    }
}
