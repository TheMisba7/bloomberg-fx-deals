package org.boolmberg.datawarehouse.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FxDealDTO {
    @NotBlank(message = "Deal unique ID is required")
    @Size(max = 255, message = "Deal unique ID must not exceed 255 characters")
    private String dealId;

    @NotBlank(message = "From currency ISO code is required")
    @Pattern(regexp = "^[A-Z]{3}$", message = "From currency must be a valid 3-letter ISO code")
    private String currencyFrom;

    @NotBlank(message = "To currency ISO code is required")
    @Pattern(regexp = "^[A-Z]{3}$", message = "To currency must be a valid 3-letter ISO code")
    private String currencyTo;

    @NotNull(message = "Deal timestamp is required")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dealTimestamp;

    @NotNull(message = "Deal amount is required")
    @DecimalMin(value = "0.0001", inclusive = true, message = "Deal amount must be greater than 0")
    @Digits(integer = 15, fraction = 4, message = "Deal amount format is invalid")
    private BigDecimal dealAmount;

    @NotNull(message = "Exchange rate is required")
    private Double exchangeRate;
}
