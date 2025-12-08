package org.linhtk.orchestrator.model.chat;

import jakarta.persistence.*;
import lombok.*;
import org.linhtk.common.model.AbstractAuditEntity;

@Entity
@Table(name = "conversation")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Conversation extends AbstractAuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public String id;

    public String name;
    public String sessionId;
}
