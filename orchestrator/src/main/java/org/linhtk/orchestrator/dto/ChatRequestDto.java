package org.linhtk.orchestrator.dto;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ChatRequestDto {
    private String question;
    private String conversationId;
    private List<MultipartFile> files;
    private String agentId;
}
