package com.vtex.catalog.worker.application.usecase.iterator;

import com.vtex.catalog.worker.application.domain.processing.ProductEntryPayload;
import com.vtex.catalog.worker.application.domain.processing.ProductEntryStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductEntryChainExecutor {

    private final ProductEntryChainFactory factory;

    public ProductEntryStatus execute(ProductEntryPayload product) {
        var context = ProductEntryContext.of(product);
        this.factory.getChain().handle(context);

        return context.getStatus();
    }
}
