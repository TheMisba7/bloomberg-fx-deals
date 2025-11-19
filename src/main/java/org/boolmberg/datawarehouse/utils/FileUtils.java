package org.boolmberg.datawarehouse.utils;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import lombok.extern.slf4j.Slf4j;
import org.boolmberg.datawarehouse.dto.FxDealDTO;
import org.boolmberg.datawarehouse.exception.InvalidFileException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@Slf4j
public final class FileUtils {

    private static final DateTimeFormatter[] DATE_FORMATTERS = {
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"),
            DateTimeFormatter.ISO_LOCAL_DATE_TIME
    };

    private FileUtils() {}


    public static void pareFile(MultipartFile file, List<FxDealDTO> target) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("file is invalid");
        }

        String contentType = file.getContentType();
        if (contentType == null ||
                !(contentType.equalsIgnoreCase("text/csv") ||
                        contentType.equalsIgnoreCase("application/vnd.ms-excel") ||
                        contentType.equalsIgnoreCase("text/plain"))) {

            throw new InvalidFileException("Invalid file type. Expected CSV");
        }

        try (CSVReader csvReader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
            List<String[]> records = csvReader.readAll();

            if (records.isEmpty()) {
                log.warn("CSV file is empty");
                return;
            }

            // Skip header row
            for (int i = 1; i < records.size(); i++) {
                String[] row = records.get(i);

                if (row.length == 0 || isEmptyRow(row)) {
                    log.debug("Skipping empty row at line {}", i + 1);
                    continue;
                }

                try {
                    FxDealDTO deal = parseRow(row, i + 1);
                    target.add(deal);
                } catch (Exception e) {
                    log.error("Error parsing row {}: {}", i + 1, e.getMessage());
                    // Create a deal with the error for proper error handling
                    FxDealDTO errorDeal = FxDealDTO.builder()
                            .dealId(row.length > 0 ? row[0] : "UNKNOWN_ROW_" + (i + 1))
                            .build();
                    target.add(errorDeal);
                }
            }

            log.info("Successfully parsed {} deals from CSV", target.size());

        } catch (CsvException e) {
            log.error("Error reading CSV file", e);
            throw new RuntimeException("Failed to parse CSV file: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean isEmptyRow(String[] row) {
        for (String cell : row) {
            if (cell != null && !cell.trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private static FxDealDTO parseRow(String[] row, int rowNumber) {
        if (row.length < 5) {
            throw new IllegalArgumentException(
                    "Row must contain at least 5 columns. Found: " + row.length
            );
        }

        String dealUniqueId = row[0] != null ? row[0].trim() : null;
        String fromCurrency = row[1] != null ? row[1].trim().toUpperCase() : null;
        String toCurrency = row[2] != null ? row[2].trim().toUpperCase() : null;
        LocalDateTime dealTimestamp = parseTimestamp(row[3], rowNumber);
        BigDecimal dealAmount = parseAmount(row[4], rowNumber);

        return FxDealDTO.builder()
                .dealId(dealUniqueId)
                .currencyFrom(fromCurrency)
                .currencyTo(toCurrency)
                .dealTimestamp(dealTimestamp)
                .dealAmount(dealAmount)
                .build();
    }

    private static LocalDateTime parseTimestamp(String timestampStr, int rowNumber) {
        if (timestampStr == null || timestampStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Timestamp is empty at row " + rowNumber);
        }

        String trimmed = timestampStr.trim();

        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                return LocalDateTime.parse(trimmed, formatter);
            } catch (DateTimeParseException e) {
                // Try next formatter
            }
        }

        throw new IllegalArgumentException(
                "Invalid timestamp format at row " + rowNumber + ": " + timestampStr
        );
    }

    private static BigDecimal parseAmount(String amountStr, int rowNumber) {
        if (amountStr == null || amountStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Amount is empty at row " + rowNumber);
        }

        try {
            return new BigDecimal(amountStr.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Invalid amount format at row " + rowNumber + ": " + amountStr, e
            );
        }
    }
}
