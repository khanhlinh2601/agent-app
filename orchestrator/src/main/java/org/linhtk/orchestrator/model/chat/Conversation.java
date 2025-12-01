package org.linhtk.orchestrator.model.chat;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.linhtk.common.model.AbstractAuditEntity;
import org.linhtk.orchestrator.model.agent.Agent;

@Entity
@Table(name = "conversation")
@IdClass(Agent.class)
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
