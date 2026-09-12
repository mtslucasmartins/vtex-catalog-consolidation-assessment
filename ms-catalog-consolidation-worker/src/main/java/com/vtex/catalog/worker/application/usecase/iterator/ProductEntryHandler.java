package com.vtex.catalog.worker.application.usecase.iterator;

import com.vtex.catalog.worker.application.common.stereotypes.Handler;

public interface ProductEntryHandler extends Handler<ProductEntryContext> {

    void setNext(ProductEntryHandler next);
}
