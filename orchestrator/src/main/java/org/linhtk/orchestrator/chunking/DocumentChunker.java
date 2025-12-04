package org.linhtk.orchestrator.chunking;

import lombok.extern.slf4j.Slf4j;
import org.linhtk.orchestrator.config.ChunkerProperties;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Document chunker component responsible for splitting documents into manageable chunks
 * for vector storage and AI processing. Uses profile-based configuration for different
 * document types with prebuilt splitters.
 * 
 * Design decisions:
 * - Uses TokenTextSplitter from Spring AI for token-aware chunking
 * - Lazy initialization of splitters for better performance
 * - Profile-based configuration allows customization via application.properties
 */
@Component
@Slf4j
public class DocumentChunker {

    private final DocumentTextExtractor textExtractor;
    private final ChunkerProfileDetector profileDetector;
    
    /**
     * Raw profile configurations loaded from application.properties
     */
    private final Map<String, ChunkerProperties.ChunkerProfile> profileConfigs;
    
    /**
     * Default prebuilt splitters - lazily initialized for better startup performance
     */
    private final Map<String, TokenTextSplitter> defaultSplitters = new HashMap<>();

    public DocumentChunker(ChunkerProperties chunkerProperties,
                          DocumentTextExtractor textExtractor,
                          ChunkerProfileDetector profileDetector) {
        this.textExtractor = textExtractor;
        this.profileDetector = profileDetector;
        this.profileConfigs = chunkerProperties.getProfiles();
    }

    /**
     * Split file into token-aware chunks with optional runtime overrides.
     * Follows the functional programming style similar to the Kotlin version.
     * 
     * @param file MultipartFile to process
     * @param profileOverride Explicit profile name (optional)
     * @return List of Document chunks with metadata
     */
    public List<Document> splitDocumentIntoChunks(MultipartFile file, String profileOverride ) {
        // Early return for empty files
        if (file == null || file.isEmpty()) {
            log.debug("File is null or empty, returning empty list");
            return List.of();
        }

        String fileName = Optional.ofNullable(file.getOriginalFilename()).orElse("unknown");
        String extension = extractFileExtension(fileName);
        
        try {
            // Extract text content using the text extractor
            String text = textExtractor.extract(file, extension);
            if (text == null || text.isBlank()) {
                log.debug("Extracted text is blank for file: {}", fileName);
                return List.of();
            }

            // Determine chunking profile - use override or detect automatically
            String chosenProfile = Optional.ofNullable(profileOverride)
                .orElseGet(() -> profileDetector.detect(text, extension));
            
            // Get or create the appropriate splitter
            TokenTextSplitter splitter = getOrCreateSplitter(chosenProfile);
            
            // Create enriched metadata for tracking and debugging
            Map<String, Object> metadata = createMetadata(fileName, extension, file.getSize(), chosenProfile);
            
            // Create document with text and metadata
            Document document = Document.builder()
                .text(text)
                .metadata(metadata)
                .build();

            // Apply splitter and return chunks
            List<Document> chunks = splitter.apply(List.of(document));
            
            log.debug("Successfully split document '{}' into {} chunks using profile '{}'", 
                     fileName, chunks.size(), chosenProfile);
            
            return chunks;

        } catch (Exception e) {
            log.error("Failed to split document '{}': {}", fileName, e.getMessage(), e);
            throw new RuntimeException("Document chunking failed for file: " + fileName, e);
        }
    }


    /**
     * Get or lazily create a TokenTextSplitter for the specified profile.
     * Uses caching to avoid recreating splitters for better performance.
     * 
     * @param profileName The chunking profile name
     * @return Configured TokenTextSplitter
     * @throws IllegalArgumentException if profile is unknown
     */
    private TokenTextSplitter getOrCreateSplitter(String profileName) {
        return defaultSplitters.computeIfAbsent(profileName, this::createSplitterForProfile);
    }

    /**
     * Factory method to create TokenTextSplitter instances based on profile configuration.
     * Maps profile configuration to Spring AI TokenTextSplitter parameters.
     * 
     * @param profileName The profile name to create splitter for
     * @return New TokenTextSplitter instance
     * @throws IllegalArgumentException if profile is unknown
     */
    private TokenTextSplitter createSplitterForProfile(String profileName) {
        ChunkerProperties.ChunkerProfile config = profileConfigs.get(profileName);
        if (config == null) {
            log.warn("Unknown splitter profile '{}', falling back to default", profileName);
            config = profileConfigs.get("default");
            if (config == null) {
                throw new IllegalArgumentException("Unknown splitter profile and no default available: " + profileName);
            }
        }

        log.debug("Creating TokenTextSplitter for profile '{}' with chunkSize: {}, overlap: {}", 
                 profileName, config.getChunkSize(), config.getChunkOverlap());

        return TokenTextSplitter.builder()
            .withChunkSize(config.getChunkSize())
            .withMinChunkSizeChars(config.getMinChunkSizeChars())
            .withKeepSeparator(config.isKeepSeparator())
            .build();
    }

    /**
     * Extract file extension from filename, handling edge cases.
     * 
     * @param fileName The original filename
     * @return Lowercase file extension or empty string
     */
    private String extractFileExtension(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return "";
        }
        
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) {
            return "";
        }
        
        return fileName.substring(lastDotIndex + 1).toLowerCase();
    }

    /**
     * Create comprehensive metadata for chunks to support debugging and analytics.
     * Includes source information, processing parameters, and profiling data.
     * 
     * @param fileName The source filename
     * @param extension The file extension
     * @param fileSize The original file size
     * @param chosenProfile The chunking profile used
     * @return Metadata map
     */
    private Map<String, Object> createMetadata(String fileName, String extension, long fileSize, String chosenProfile) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("source", fileName);
        metadata.put("extension", extension);
        metadata.put("fileSize", fileSize);
        metadata.put("profile", chosenProfile);
        metadata.put("processingTimestamp", System.currentTimeMillis());
        
        // Add profile configuration details for debugging
        ChunkerProperties.ChunkerProfile config = profileConfigs.get(chosenProfile);
        if (config != null) {
            metadata.put("chunkSize", config.getChunkSize());
            metadata.put("chunkOverlap", config.getChunkOverlap());
            metadata.put("keepSeparator", config.isKeepSeparator());
        }
        
        return metadata;
    }


}
