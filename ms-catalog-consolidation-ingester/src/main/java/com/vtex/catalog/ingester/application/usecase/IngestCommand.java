package com.vtex.catalog.ingester.application.usecase;

import com.vtex.catalog.ingester.application.common.stereotypes.Intent;
import lombok.Builder;
import lombok.Getter;

import java.io.InputStream;

@Getter
@Builder
public final class IngestCommand implements Intent {

    private final String fileName;
    private final long contentLength;
    private final InputStream content;
}
