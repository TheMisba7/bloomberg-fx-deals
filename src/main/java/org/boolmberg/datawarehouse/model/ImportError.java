package org.boolmberg.datawarehouse.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

import java.time.LocalDateTime;

@Entity
@Table(name = "import_errors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImportError {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "row_number")
    private Integer rowNumber;

    @Column(name = "deal_id")
    private String dealId;

    @Column(name = "error_message", nullable = false, columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "error_type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private ImportErrorType errorType;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
