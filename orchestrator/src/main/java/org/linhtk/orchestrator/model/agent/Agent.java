package org.linhtk.orchestrator.model.agent;

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

@Entity
@Table(name = "agent")
@IdClass(Agent.class)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Agent extends AbstractAuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public String id;
    public String name;
    public String description;
    public String instructions;
    public String providerName;
    public String providerModelName;
    public String providerEmbeddingModelName;
    public String providerEndpoint;
    public String providerApiKey;
    public boolean isPublished;
    public boolean isDefault;
}
