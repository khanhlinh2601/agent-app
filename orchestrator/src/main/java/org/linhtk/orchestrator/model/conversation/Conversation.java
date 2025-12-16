package org.linhtk.orchestrator.model.conversation;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.linhtk.common.model.AbstractAuditEntity;


@Entity
@Table(name = "conversation")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Conversation extends AbstractAuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String name;
    private String sessionId;
}
