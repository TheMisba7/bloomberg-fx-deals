package org.boolmberg.datawarehouse.dao;

import org.boolmberg.datawarehouse.model.ImportError;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImportErrorTypeDao extends JpaRepository<ImportError, Long> {
}
