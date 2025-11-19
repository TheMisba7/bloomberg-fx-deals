package org.boolmberg.datawarehouse.app;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.boolmberg.datawarehouse.dao.ImportErrorTypeDao;
import org.boolmberg.datawarehouse.dto.FxDealDTO;
import org.boolmberg.datawarehouse.dto.ImportSummary;
import org.boolmberg.datawarehouse.exception.DuplicateDealException;
import org.boolmberg.datawarehouse.exception.InvalidFileException;
import org.boolmberg.datawarehouse.exception.ValidationException;
import org.boolmberg.datawarehouse.model.ImportErrorType;
import org.boolmberg.datawarehouse.service.ErrorService;
import org.boolmberg.datawarehouse.service.FxDealService;
import org.boolmberg.datawarehouse.utils.FileUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class FxDealApp {

    private final FxDealService fxDealService;
    private final ErrorService errorService;


    public ImportSummary importDeal(FxDealDTO dto) {
        ImportSummary importSummary = ImportSummary.builder()
                .totalRecords(1)
                .successfulImports(0)
                .failedImports(0)
                .duplicateImports(0)
                .build();
        fxDealService.importDeal(dto);
        importSummary.setSuccessfulImports(1);
        return importSummary;
    }
    public ImportSummary importDeals(List<FxDealDTO> deals) {
        ImportSummary importSummary = ImportSummary.builder()
                .totalRecords(deals.size())
                .successfulImports(0)
                .failedImports(0)
                .duplicateImports(0)
                .build();

        for (int i = 0; i < deals.size(); i++) {
            FxDealDTO deal = deals.get(i);
            int rowNumber = i + 1;
            try {
                fxDealService.importDeal(deal);
                importSummary.setSuccessfulImports(importSummary.getSuccessfulImports() + 1);
            } catch (ValidationException e) {
                log.error(e.getMessage(), e);
                importSummary.setFailedImports(importSummary.getFailedImports() + 1);
                importSummary.addError(ImportSummary.ImportErrorDto.builder()
                        .dealId(deal.getDealId())
                        .errorMessage(e.getMessage())
                        .build());
                errorService.addImportError(rowNumber, deal.getDealId(), e.getMessage(), ImportErrorType.VALIDATION);
            } catch (DuplicateDealException e) {
                log.error(e.getMessage(), e);
                importSummary.setDuplicateImports(importSummary.getDuplicateImports() + 1);
                importSummary.addError(ImportSummary.ImportErrorDto.builder()
                        .dealId(deal.getDealId())
                        .errorMessage(e.getMessage())
                        .build());
                errorService.addImportError(rowNumber, deal.getDealId(), e.getMessage(), ImportErrorType.DUPLICATE);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                importSummary.setFailedImports(importSummary.getFailedImports() + 1);
                importSummary.addError(ImportSummary.ImportErrorDto.builder()
                        .dealId(deal.getDealId())
                        .errorMessage("internal error").build());
                errorService.addImportError(rowNumber, deal.getDealId(), e.getMessage(), ImportErrorType.UNKNOWN);
            }
        }

        return importSummary;
    }

    public ImportSummary uploadCsv(MultipartFile file) {
        List<FxDealDTO> deals = new ArrayList<>();
        FileUtils.pareFile(file, deals);
        return importDeals(deals);
    }
}
