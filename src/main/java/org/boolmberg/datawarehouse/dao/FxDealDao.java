package org.boolmberg.datawarehouse.dao;

import org.boolmberg.datawarehouse.dto.FxDealDTO;
import org.boolmberg.datawarehouse.model.FxDeal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FxDealDao extends JpaRepository<FxDeal, Long> {

    boolean existsByFxDealId(String fxDealId);

    Optional<FxDeal> findByFxDealId(String fxDealId);
}
