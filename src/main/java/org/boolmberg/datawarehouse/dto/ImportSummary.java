package org.boolmberg.datawarehouse.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.boolmberg.datawarehouse.model.ImportError;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImportSummary {
    private int totalRecords;
    private int successfulImports;
    private int failedImports;
    private int duplicateImports;


    public void addError(ImportError error) {
        if (errors == null)
            errors = new ArrayList<>();
        errors.add(error);
    }

    @Builder.Default
    private List<ImportError> errors = new ArrayList<>();
}
