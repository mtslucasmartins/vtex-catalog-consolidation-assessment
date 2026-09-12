package com.vtex.catalog.worker.application.usecase.iterator;

public abstract class AbstractProductEntryHandler implements ProductEntryHandler {

    private ProductEntryHandler next;

    @Override
    public void setNext(ProductEntryHandler next) {
        this.next = next;
    }

    protected void next(ProductEntryContext context) {
        if (next != null) {
            next.handle(context);
        }
    }
}
