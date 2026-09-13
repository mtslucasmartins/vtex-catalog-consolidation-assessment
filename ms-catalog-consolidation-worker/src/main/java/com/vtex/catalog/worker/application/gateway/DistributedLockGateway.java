package com.vtex.catalog.worker.application.gateway;

import java.util.concurrent.locks.Lock;

public interface DistributedLockGateway {

    Lock acquire(String resourceKey);
}
