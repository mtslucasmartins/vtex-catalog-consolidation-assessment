package com.vtex.catalog.worker.application.common.stereotypes;

public interface Handler<C> {

    void handle(C context);

}
