package com.vtex.catalog.ingester.application.common.stereotypes;

public interface UseCase<I extends Intent, R> {

    R execute(I intent);
}
