package com.vtex.catalog.worker.application.gateway;

public interface DistributedLockGateway {

    DistributedLock acquire(String resourceKey);

    void release(DistributedLock lock);

    record DistributedLock(String resourceKey, String token) {
    }
}
