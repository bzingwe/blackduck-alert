package com.synopsys.integration.alert.database.repository.descriptor;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.database.entity.descriptor.DescriptorFieldRelation;
import com.synopsys.integration.alert.database.relation.key.DescriptorFieldRelationPK;

@Component
public interface DescriptorFieldRepository extends JpaRepository<DescriptorFieldRelation, DescriptorFieldRelationPK> {
    List<DescriptorFieldRelation> findByDescriptorId(final Long descriptorId);

    List<DescriptorFieldRelation> findByFieldId(final Long fieldId);
}
