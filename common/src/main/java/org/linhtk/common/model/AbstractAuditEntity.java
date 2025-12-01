package org.linhtk.common.model;

import jakarta.persistence.EntityListeners;
import java.time.ZonedDateTime;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.linhtk.common.model.listener.CustomAuditingEntityListener;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;

@Getter
@Setter
@EntityListeners(CustomAuditingEntityListener.class)
public class AbstractAuditEntity {

    @CreationTimestamp
    private ZonedDateTime createdAt;

    @CreatedBy
    private String createdBy;

    @UpdateTimestamp
    private ZonedDateTime updatedAt;

    @LastModifiedBy
    private String updatedBy;

    private boolean deleted = false;
}
