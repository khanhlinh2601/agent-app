package org.linhtk.orchestrator.chunking;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.List;

/**
 * Component responsible for detecting the appropriate chunking profile based on file content and extension.
 * Uses various heuristics to determine whether content should be split using markdown, code, semantic,
 * or sentence-based chunking strategies.
 */
@Component
public class ChunkerProfileDetector {

    // Thresholds for text length analysis
    private static final int SMALL_TEXT_THRESHOLD = 2_000;
    private static final int MEDIUM_TEXT_THRESHOLD = 10_000;
    private static final int CSV_SAMPLE_LINES = 5;

    // Map file extensions to splitter profiles
    private static final Map<String, String> EXTENSION_TO_PROFILE = Map.ofEntries(
        // Markdown-like formats
        Map.entry("txt", "markdown"),
        Map.entry("md", "markdown"),
        // Code files
        Map.entry("java", "code"),
        Map.entry("kt", "code"),
        Map.entry("js", "code"),
        Map.entry("ts", "code"),
        Map.entry("py", "code"),
        Map.entry("cpp", "code"),
        Map.entry("c", "code"),
        Map.entry("go", "code"),
        // Data formats → semantic (meaning-based)
        Map.entry("json", "semantic"),
        Map.entry("csv", "semantic"),
        Map.entry("xml", "semantic"),
        // PDFs & DOCX often require larger chunking → semantic
        Map.entry("pdf", "semantic"),
        Map.entry("docx", "semantic")
    );

    /**
     * Detect chunking profile based on file extension first, with fallback to text analysis.
     * This is the primary entry point for profile detection.
     *
     * @param text      The text content to analyze
     * @param extension The file extension (can be null)
     * @return The detected chunking profile ("markdown", "code", "semantic", or "sentence")
     */
    public String detect(String text, String extension) {
        if (extension != null) {
            String profile = EXTENSION_TO_PROFILE.get(extension.toLowerCase());
            if (profile != null) {
                return profile;
            }
        }
        return detectFromText(text);
    }

    /**
     * Detect chunking profile based on textual patterns and content characteristics.
     * Uses various heuristics to analyze the structure and content of the text.
     *
     * @param text The text content to analyze
     * @return The detected chunking profile
     */
    public String detectFromText(String text) {
        String trimmed = text.stripLeading();

        // Check specific content types first
        if (looksLikeMarkdown(trimmed)) {
            return "markdown";
        }

        if (looksLikeCode(trimmed)) {
            return "code";
        }

        if (looksLikeCsv(trimmed) || looksLikeJson(trimmed) || looksLikeXml(trimmed)) {
            return "semantic";
        }

        // Length-based analysis for general text
        if (trimmed.length() < SMALL_TEXT_THRESHOLD) {
            // Very short text → treat as semantic (safe approach)
            return "semantic";
        }

        if (trimmed.length() < MEDIUM_TEXT_THRESHOLD) {
            // Medium text → sentence-based splitting is usually ideal
            return "sentence";
        }

        // Large documents (PDFs, reports) → semantic splitting
        return "semantic";
    }

    // ---- Helper Methods for Content Type Detection -------------------------

    /**
     * Check if text appears to be JSON format.
     * Looks for object or array structure indicators.
     */
    private boolean looksLikeJson(String text) {
        return (text.startsWith("{") && text.endsWith("}")) ||
                (text.startsWith("[") && text.endsWith("]"));
    }

    /**
     * Check if text appears to be XML format.
     * Looks for opening and closing XML tags.
     */
    private boolean looksLikeXml(String text) {
        return text.startsWith("<") && text.contains("</");
    }

    /**
     * Check if text appears to be CSV format.
     * Analyzes the first few lines for comma-separated structure.
     */
    private boolean looksLikeCsv(String text) {
        if (!text.contains(",")) {
            return false;
        }

        List<String> lines = text.lines()
                .limit(CSV_SAMPLE_LINES)
                .toList();

        return lines.stream().allMatch(line -> line.contains(","));
    }

    /**
     * Check if text appears to be Markdown format.
     * Looks for common Markdown syntax elements.
     */
    private boolean looksLikeMarkdown(String text) {
        return text.contains("# ") ||
                text.contains("```") ||
                text.contains("* ") ||
                text.contains("- ");
    }

    /**
     * Check if text appears to be source code.
     * Looks for common programming language keywords and structures.
     */
    private boolean looksLikeCode(String text) {
        return text.contains("class ") ||
                text.contains("def ") ||
                text.contains("fun ") ||
                text.contains("public ") ||
                text.contains("private ") ||
                (text.contains("{") && text.contains("}"));
    }
}
