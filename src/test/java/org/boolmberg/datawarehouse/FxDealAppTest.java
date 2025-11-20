package org.boolmberg.datawarehouse;

import org.boolmberg.datawarehouse.app.FxDealApp;
import org.boolmberg.datawarehouse.dto.FxDealDTO;
import org.boolmberg.datawarehouse.dto.ImportSummary;
import org.boolmberg.datawarehouse.exception.DuplicateDealException;
import org.boolmberg.datawarehouse.exception.ValidationException;
import org.boolmberg.datawarehouse.model.FxDeal;
import org.boolmberg.datawarehouse.model.ImportErrorType;
import org.boolmberg.datawarehouse.service.ErrorService;
import org.boolmberg.datawarehouse.service.FxDealService;
import org.boolmberg.datawarehouse.utils.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class FxDealAppTest {

    @Mock
    private FxDealService fxDealService;

    @Mock
    private ErrorService errorService;

    @InjectMocks
    private FxDealApp fxDealApp;

    private FxDealDTO validDeal;

    @BeforeEach
    void setUp() {
        validDeal = FxDealDTO.builder()
                .dealId("DEAL-001")
                .currencyFrom("USD")
                .currencyTo("EUR")
                .dealAmount(new BigDecimal("10000.50"))
                .exchangeRate(0.85)
                .dealTimestamp(LocalDateTime.now())
                .build();
    }

    // ==================== importDeal (single) Tests ====================

    @Test
    void importDeal_ValidDeal_ReturnsSuccessSummary() {
        when(fxDealService.importDeal(any(FxDealDTO.class)))
                .thenReturn(new FxDeal());

        ImportSummary result = fxDealApp.importDeal(validDeal);

        assertNotNull(result);
        assertEquals(1, result.getTotalRecords());
        assertEquals(1, result.getSuccessfulImports());
        verify(fxDealService, times(1)).importDeal(validDeal);
    }

    @Test
    void importDeal_ServiceThrowsValidationException_ThrowsException() {
        doThrow(new ValidationException("Invalid currency"))
                .when(fxDealService).importDeal(any(FxDealDTO.class));

        assertThrows(ValidationException.class,
                () -> fxDealApp.importDeal(validDeal));
    }

    @Test
    void importDeal_ServiceThrowsDuplicateException_ThrowsException() {
        doThrow(new DuplicateDealException("Deal already exists"))
                .when(fxDealService).importDeal(any(FxDealDTO.class));

        assertThrows(DuplicateDealException.class,
                () -> fxDealApp.importDeal(validDeal));
    }

    // ==================== importDeals (batch) Tests ====================

    @Test
    void importDeals_AllValid_ReturnsAllSuccessful() {
        List<FxDealDTO> deals = Arrays.asList(
                createDeal("DEAL-001"),
                createDeal("DEAL-002"),
                createDeal("DEAL-003")
        );

        when(fxDealService.importDeal(any(FxDealDTO.class)))
                .thenReturn(new FxDeal());

        ImportSummary result = fxDealApp.importDeals(deals);

        assertEquals(3, result.getSuccessfulImports());
        assertTrue(result.getErrors().isEmpty());
        verify(fxDealService, times(3)).importDeal(any());
    }

    @Test
    void importDeals_OneValidationFailure_ReturnsPartialSuccess() {
        List<FxDealDTO> deals = Arrays.asList(
                createDeal("DEAL-001"),
                createDeal("DEAL-002"),
                createDeal("DEAL-003")
        );

        when(fxDealService.importDeal(argThat(dto ->
                dto.getDealId().equals("DEAL-001") || dto.getDealId().equals("DEAL-003")
        ))).thenReturn(new FxDeal());

        doThrow(new ValidationException("Invalid currency"))
                .when(fxDealService).importDeal(argThat(dto ->
                        dto.getDealId().equals("DEAL-002")));

        ImportSummary result = fxDealApp.importDeals(deals);

        assertEquals(2, result.getSuccessfulImports());
        assertEquals(1, result.getFailedImports());
        assertEquals("DEAL-002", result.getErrors().get(0).getDealId());

        verify(errorService).addImportError(eq(2), eq("DEAL-002"), anyString(),
                eq(ImportErrorType.VALIDATION));
    }

    @Test
    void importDeals_OneDuplicate_ReturnsCorrectCounts() {
        List<FxDealDTO> deals = Arrays.asList(
                createDeal("DEAL-001"),
                createDeal("DEAL-002"),
                createDeal("DEAL-003")
        );

        when(fxDealService.importDeal(argThat(dto ->
                dto.getDealId().equals("DEAL-001") || dto.getDealId().equals("DEAL-003")
        ))).thenReturn(new FxDeal());

        doThrow(new DuplicateDealException("Deal already exists"))
                .when(fxDealService).importDeal(argThat(dto ->
                        dto.getDealId().equals("DEAL-002")));

        ImportSummary result = fxDealApp.importDeals(deals);

        assertEquals(2, result.getSuccessfulImports());
        assertEquals(1, result.getDuplicateImports());
        verify(errorService).addImportError(eq(2), eq("DEAL-002"), anyString(),
                eq(ImportErrorType.DUPLICATE));
    }

    @Test
    void importDeals_MixedErrors_ReturnsCorrectCounts() {
        List<FxDealDTO> deals = Arrays.asList(
                createDeal("DEAL-001"), // success
                createDeal("DEAL-002"), // validation
                createDeal("DEAL-003"), // duplicate
                createDeal("DEAL-004")  // success
        );

        when(fxDealService.importDeal(argThat(dto ->
                dto.getDealId().equals("DEAL-001") || dto.getDealId().equals("DEAL-004")
        ))).thenReturn(new FxDeal());

        doThrow(new ValidationException("Invalid amount"))
                .when(fxDealService).importDeal(argThat(dto ->
                        dto.getDealId().equals("DEAL-002")));

        doThrow(new DuplicateDealException("Deal exists"))
                .when(fxDealService).importDeal(argThat(dto ->
                        dto.getDealId().equals("DEAL-003")));

        ImportSummary result = fxDealApp.importDeals(deals);

        assertEquals(2, result.getSuccessfulImports());
        assertEquals(1, result.getFailedImports());
        assertEquals(1, result.getDuplicateImports());

        verify(errorService).addImportError(eq(2), eq("DEAL-002"), anyString(),
                eq(ImportErrorType.VALIDATION));
        verify(errorService).addImportError(eq(3), eq("DEAL-003"), anyString(),
                eq(ImportErrorType.DUPLICATE));
    }

    @Test
    void importDeals_EmptyList_ReturnsEmptySummary() {
        ImportSummary result = fxDealApp.importDeals(List.of());

        assertEquals(0, result.getTotalRecords());
        verify(fxDealService, never()).importDeal(any());
    }

    @Test
    void importDeals_AllFail_ReturnsAllFailures() {
        List<FxDealDTO> deals = Arrays.asList(
                createDeal("A"),
                createDeal("B")
        );

        doThrow(new ValidationException("Invalid"))
                .when(fxDealService).importDeal(any());

        ImportSummary result = fxDealApp.importDeals(deals);

        assertEquals(2, result.getFailedImports());
    }

    // ==================== uploadCsv Tests ====================

    @Test
    void uploadCsv_ValidFile_ParsesAndImportsDeals() {
        MultipartFile file = mock(MultipartFile.class);
        List<FxDealDTO> parsed = Arrays.asList(createDeal("1"), createDeal("2"));

        try (MockedStatic<FileUtils> mock = mockStatic(FileUtils.class)) {
            mock.when(() -> FileUtils.pareFile(eq(file), anyList()))
                    .thenAnswer(inv -> {
                        List<FxDealDTO> list = inv.getArgument(1);
                        list.addAll(parsed);
                        return null;
                    });

            when(fxDealService.importDeal(any())).thenReturn(new FxDeal());

            ImportSummary result = fxDealApp.uploadCsv(file);

            assertEquals(2, result.getSuccessfulImports());
            verify(fxDealService, times(2)).importDeal(any());
        }
    }

    @Test
    void uploadCsv_FileWithErrors_ReturnsPartialSuccess() {
        MultipartFile file = mock(MultipartFile.class);
        List<FxDealDTO> parsed = Arrays.asList(
                createDeal("1"),
                createDeal("2"),
                createDeal("3")
        );

        try (MockedStatic<FileUtils> mock = mockStatic(FileUtils.class)) {
            mock.when(() -> FileUtils.pareFile(eq(file), anyList()))
                    .thenAnswer(inv -> {
                        List<FxDealDTO> list = inv.getArgument(1);
                        list.addAll(parsed);
                        return null;
                    });

            when(fxDealService.importDeal(argThat(d -> d.getDealId().equals("1") || d.getDealId().equals("3"))))
                    .thenReturn(new FxDeal());

            doThrow(new ValidationException("Invalid"))
                    .when(fxDealService).importDeal(argThat(d -> d.getDealId().equals("2")));

            ImportSummary result = fxDealApp.uploadCsv(file);

            assertEquals(2, result.getSuccessfulImports());
            assertEquals(1, result.getFailedImports());
        }
    }

    // ==================== Helper ====================

    private FxDealDTO createDeal(String id) {
        return FxDealDTO.builder()
                .dealId(id)
                .currencyFrom("USD")
                .currencyTo("EUR")
                .dealAmount(new BigDecimal("10000"))
                .exchangeRate(0.85)
                .dealTimestamp(LocalDateTime.now())
                .build();
    }
}
