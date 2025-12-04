package org.linhtk.orchestrator.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "chunker")
@Data
public class ChunkerProperties {
    
    /**
     * Default chunk size for document processing
     */
    public static final int DEFAULT_CHUNK_SIZE = 500;
    
    /**
     * Minimum chunk size in characters to ensure meaningful content
     */
    public static final int DEFAULT_MIN_CHUNK_SIZE_CHARS = 300;
    
    /**
     * Minimum chunk length required before embedding processing
     */
    public static final int DEFAULT_MIN_CHUNK_LENGTH_TO_EMBED = 10;
    
    /**
     * Maximum number of chunks to prevent memory overflow
     */
    public static final int DEFAULT_MAX_NUM_CHUNKS = 1000;

    /**
     * Profile-based chunker configurations loaded from application.properties
     * Example configuration:
     * chunker.profiles.markdown.chunkSize=500
     * chunker.profiles.code.chunkSize=1000
     */
    private Map<String, ChunkerProfile> profiles = new HashMap<>();

    /**
     * Initialize default profiles if none are configured
     */
    public ChunkerProperties() {
        initializeDefaultProfiles();
    }

    /**
     * Configuration for a specific chunking profile
     */
    @Data
    public static class ChunkerProfile {
        private int chunkSize = DEFAULT_CHUNK_SIZE;
        private int minChunkSizeChars = DEFAULT_MIN_CHUNK_SIZE_CHARS;
        private int minChunkLengthToEmbed = DEFAULT_MIN_CHUNK_LENGTH_TO_EMBED;
        private int maxNumChunks = DEFAULT_MAX_NUM_CHUNKS;
        private boolean keepSeparator = true;
        private int chunkOverlap = 50;

        public ChunkerProfile() {
            // Default constructor
        }

        public ChunkerProfile(int chunkSize, int minChunkSizeChars, int chunkOverlap, boolean keepSeparator) {
            this.chunkSize = chunkSize;
            this.minChunkSizeChars = minChunkSizeChars;
            this.chunkOverlap = chunkOverlap;
            this.keepSeparator = keepSeparator;
        }
    }

    /**
     * Initialize sensible default profiles for different content types
     * These can be overridden via application.properties
     */
    private void initializeDefaultProfiles() {
        // Markdown profile - smaller chunks to preserve structure
        profiles.put("markdown", new ChunkerProfile(DEFAULT_CHUNK_SIZE, DEFAULT_MIN_CHUNK_SIZE_CHARS, 50, true));
        
        // Code profile - larger chunks to maintain function boundaries
        profiles.put("code", new ChunkerProfile(DEFAULT_CHUNK_SIZE * 2, DEFAULT_MIN_CHUNK_SIZE_CHARS, 100, true));
        
        // Semantic profile - large chunks for complex documents
        profiles.put("semantic", new ChunkerProfile(DEFAULT_CHUNK_SIZE * 3, DEFAULT_MIN_CHUNK_SIZE_CHARS * 2, 150, false));
        
        // Sentence profile - standard chunks with sentence awareness
        profiles.put("sentence", new ChunkerProfile(DEFAULT_CHUNK_SIZE, DEFAULT_MIN_CHUNK_SIZE_CHARS, 75, true));
        
        // Default fallback profile
        profiles.put("default", new ChunkerProfile(DEFAULT_CHUNK_SIZE, DEFAULT_MIN_CHUNK_SIZE_CHARS, 50, true));
    }

    /**
     * Get a profile by name, falling back to default if not found
     */
    public ChunkerProfile getProfile(String profileName) {
        return profiles.getOrDefault(profileName, profiles.get("default"));
    }
}
