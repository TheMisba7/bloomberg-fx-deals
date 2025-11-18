package org.boolmberg.datawarehouse.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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


    public void addError(ImportErrorDto importErrorDto) {
        if (errors == null)
            errors = new ArrayList<>();
        errors.add(importErrorDto);
    }

    @Builder.Default
    private List<ImportErrorDto> errors = new ArrayList<>();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ImportErrorDto {
        private Integer rowNumber;
        private String dealId;
        private String errorMessage;
        private String errorType;
    }
}
