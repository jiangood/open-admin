package io.admin.framework.data.id;

import io.admin.framework.data.domain.BaseNoIdEntity;
import io.admin.framework.data.id.ann.GeneratePrefixedSequence;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Entity
@Data
public class TestEntity1  extends BaseNoIdEntity {





    @Id
    @GeneratePrefixedSequence(prefix = "BOOK")
    private String id;

    private String name;

    @Override
    public boolean isNew() {
        return getId() == null;
    }
}
