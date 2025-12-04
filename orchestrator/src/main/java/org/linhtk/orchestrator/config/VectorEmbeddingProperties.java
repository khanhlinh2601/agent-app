package org.linhtk.orchestrator.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "chunker")
public class VectorEmbeddingProperties {
    public static final int DEFAULT_EMBEDDING_DIMENSION = 1536;
}
