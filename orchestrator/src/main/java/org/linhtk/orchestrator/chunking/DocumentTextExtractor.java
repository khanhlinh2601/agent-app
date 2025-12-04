package org.linhtk.orchestrator.chunking;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Document text extractor component responsible for extracting text content from various file formats.
 * Uses Spring AI's PagePdfDocumentReader for PDF text extraction and direct reading for plain text files.
 * Follows Spring AI patterns for document processing and chunking.
 */
@Component
public class DocumentTextExtractor {
    
    private static final int MAX_CONTENT_LENGTH = 10 * 1024 * 1024; // 10MB limit for extracted text
    
    /**
     * Extracts text content from uploaded file based on file extension.
     * Returns the content as a single string for further processing.
     * 
     * @param file the multipart file to extract text from
     * @param ext the file extension (e.g., "pdf", "txt")
     * @return extracted text content as string
     * @throws IllegalArgumentException if file or extension is invalid
     * @throws RuntimeException if extraction fails
     */
    public String extract(MultipartFile file, String ext) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be null or empty");
        }
        
        if (ext == null || ext.trim().isEmpty()) {
            throw new IllegalArgumentException("File extension cannot be null or empty");
        }
        
        String normalizedExt = ext.toLowerCase().trim();
        
        return switch (normalizedExt) {
            case "pdf" -> extractPdf(file);
            case "txt", "text" -> extractPlainText(file);
            default -> throw new IllegalArgumentException("Unsupported file extension: " + ext);
        };
    }

    /**
     * Extracts text content from PDF files using Spring AI's PagePdfDocumentReader.
     * This approach leverages Spring AI's document processing capabilities for better integration
     * with the AI pipeline and provides structured document handling.
     * 
     * @param file the PDF file to extract text from
     * @return extracted text content as a single concatenated string
     * @throws RuntimeException if PDF extraction fails
     */
    private String extractPdf(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            // Create InputStreamResource for Spring AI PagePdfDocumentReader
            InputStreamResource resource = new InputStreamResource(inputStream);
            
            // Use Spring AI's PagePdfDocumentReader with default configuration
            // This reader automatically handles page-by-page extraction and creates Document objects
            PagePdfDocumentReader pdfReader = new PagePdfDocumentReader(resource);
            
            // Extract all documents (pages) from the PDF
            List<Document> documents = pdfReader.get();
            
            if (documents.isEmpty()) {
                throw new RuntimeException("No content found in PDF file or PDF contains no readable pages");
            }
            
            // Concatenate all page content into a single string
            StringBuilder contentBuilder = new StringBuilder();
            for (Document document : documents) {
                String pageContent = document.getText();
                if (pageContent != null && !pageContent.trim().isEmpty()) {
                    contentBuilder.append(pageContent.trim()).append("\n\n");
                }
            }
            
            String extractedText = contentBuilder.toString().trim();
            
            if (extractedText.isEmpty()) {
                throw new RuntimeException("No readable text content found in PDF file");
            }
            
            if (extractedText.length() > MAX_CONTENT_LENGTH) {
                throw new RuntimeException("Extracted PDF content exceeds maximum size limit of " + MAX_CONTENT_LENGTH + " characters");
            }
            
            return extractedText;
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to read PDF file: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to process PDF file with Spring AI reader: " + e.getMessage(), e);
        }
    }

    /**
     * Extracts text content from plain text files with proper encoding handling.
     * Supports UTF-8 encoding with fallback to system default.
     * 
     * @param file the text file to extract content from
     * @return file content as string
     * @throws RuntimeException if text extraction fails
     */
    private String extractPlainText(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            byte[] bytes = inputStream.readAllBytes();
            
            if (bytes.length == 0) {
                throw new RuntimeException("Text file is empty");
            }
            
            if (bytes.length > MAX_CONTENT_LENGTH) {
                throw new RuntimeException("Text file too large. Maximum size: " + MAX_CONTENT_LENGTH + " bytes");
            }
            
            // Try UTF-8 encoding first, fallback to system default
            String content = new String(bytes, StandardCharsets.UTF_8).trim();
            
            // Validate that the content is valid UTF-8
            if (content.contains("\ufffd")) {
                // If replacement characters found, try with system default encoding
                content = new String(bytes).trim();
            }
            
            if (content.isEmpty()) {
                throw new RuntimeException("No readable text content found in file");
            }
            
            return content;
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to read text file: " + e.getMessage(), e);
        }
    }
}
