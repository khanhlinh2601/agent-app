package org.linhtk.orchestrator.model.conversation;

import jakarta.persistence.*;
import lombok.*;
import org.linhtk.common.model.AbstractAuditEntity;

@Entity
@Table(name = "chat_message")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ChatMessage extends AbstractAuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String conversationId;
    private String content;
    private String agentId;
    private int type; // 0=USER, 1=ASSISTANT, 2=SYSTEM

}
