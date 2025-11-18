package org.boolmberg.datawarehouse.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.boolmberg.datawarehouse.dao.ImportErrorTypeDao;
import org.boolmberg.datawarehouse.model.ImportError;
import org.boolmberg.datawarehouse.model.ImportErrorType;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ErrorService {

    private final ImportErrorTypeDao importErrorTypeDao;

    public void addImportError(Integer rowNumber, String dealId,
                               String message, ImportErrorType type) {
        ImportError error = ImportError.builder()
                .rowNumber(rowNumber)
                .errorMessage(message)
                .errorType(type)
                .dealId(dealId)
                .build();

        importErrorTypeDao.save(error);
    }
}
