package org.sid.usersaas.repository;

import org.sid.usersaas.entities.UsageType;
import org.sid.usersaas.enums.ServiceCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsageTypeRepository extends JpaRepository<UsageType, String> {
    boolean existsByServiceCategory(ServiceCategory category);

}
