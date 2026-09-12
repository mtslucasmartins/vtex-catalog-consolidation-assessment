package com.vtex.catalog.worker.application.common.stereotypes;

public interface UseCase<I extends Intent, R> {

    R execute(I intent);

}
