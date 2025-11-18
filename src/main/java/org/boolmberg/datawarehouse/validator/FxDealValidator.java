package org.boolmberg.datawarehouse.validator;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.extern.slf4j.Slf4j;
import org.boolmberg.datawarehouse.dto.FxDealDTO;
import org.boolmberg.datawarehouse.exception.ValidationException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.HashSet;
import java.util.Set;

@Component
@Slf4j
public class FxDealValidator {

    private static final Set<String> VALID_ISO_CODES = new HashSet<>();

    static {
        Currency.getAvailableCurrencies().forEach(currency ->
                VALID_ISO_CODES.add(currency.getCurrencyCode())
        );
    }

    public void validate(FxDealDTO dealDto) {
        log.debug("Validating FX deal: {}", dealDto.getDealId());

        validateDealId(dealDto.getDealId());
        validateCurrencyCode(dealDto.getCurrencyFrom(), "From currency");
        validateCurrencyCode(dealDto.getCurrencyTo(), "To currency");
        validateDifferentCurrencies(dealDto.getCurrencyFrom(), dealDto.getCurrencyTo());
        validateDealTimestamp(dealDto.getDealTimestamp());
        validateDealAmount(dealDto.getDealAmount());
    }

    private void validateDealId(String dealId) {
        if (dealId == null || dealId.trim().isEmpty()) {
            throw new ValidationException("Deal unique ID cannot be empty");
        }
        if (dealId.length() > 255) {
            throw new ValidationException("Deal unique ID exceeds maximum length of 255 characters");
        }
    }

    private void validateCurrencyCode(String currencyCode, String fieldName) {
        if (currencyCode == null || currencyCode.trim().isEmpty()) {
            throw new ValidationException(fieldName + " ISO code cannot be empty");
        }
        if (!currencyCode.matches("^[A-Z]{3}$")) {
            throw new ValidationException(fieldName + " must be a 3-letter uppercase ISO code");
        }
        if (!VALID_ISO_CODES.contains(currencyCode)) {
            throw new ValidationException(fieldName + " '" + currencyCode + "' is not a valid ISO 4217 currency code");
        }
    }

    private void validateDifferentCurrencies(String fromCurrency, String toCurrency) {
        if (fromCurrency != null && fromCurrency.equals(toCurrency)) {
            throw new ValidationException("From currency and To currency must be different");
        }
    }

    private void validateDealTimestamp(LocalDateTime dealTimestamp) {
        if (dealTimestamp == null) {
            throw new ValidationException("Deal timestamp cannot be null");
        }
        LocalDateTime maxAllowedTime = LocalDateTime.now().plusDays(1);
        if (dealTimestamp.isAfter(maxAllowedTime)) {
            throw new ValidationException("Deal timestamp cannot be more than 1 day in the future");
        }
    }

    private void validateDealAmount(BigDecimal dealAmount) {
        if (dealAmount == null) {
            throw new ValidationException("Deal amount cannot be null");
        }
        if (dealAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Deal amount must be greater than zero");
        }
        if (dealAmount.scale() > 4) {
            throw new ValidationException("Deal amount cannot have more than 4 decimal places");
        }
    }
}
