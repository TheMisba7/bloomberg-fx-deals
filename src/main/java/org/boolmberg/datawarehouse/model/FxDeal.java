package org.boolmberg.datawarehouse.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "fx_deals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FxDeal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "deal_id", nullable = false, unique = true)
    private String dealId;

    @Column(name = "currency_from", nullable = false, length = 3)
    private String currencyFrom;

    @Column(name = "currency_to", nullable = false, length = 3)
    private String currencyTo;

    @Column(name = "deal_timestamp", nullable = false)
    private LocalDateTime dealTimestamp;

    @Column(name = "deal_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal dealAmount;

    @Column(name = "exchange_rate", nullable = false)
    private Double exchangeRate;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}