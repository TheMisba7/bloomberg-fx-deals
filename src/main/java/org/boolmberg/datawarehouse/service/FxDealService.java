package org.boolmberg.datawarehouse.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.boolmberg.datawarehouse.dao.FxDealDao;
import org.boolmberg.datawarehouse.dto.FxDealDTO;
import org.boolmberg.datawarehouse.dto.ImportSummary;
import org.boolmberg.datawarehouse.exception.DuplicateDealException;
import org.boolmberg.datawarehouse.exception.ValidationException;
import org.boolmberg.datawarehouse.model.FxDeal;
import org.boolmberg.datawarehouse.validator.FxDealValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FxDealService {

    private final FxDealDao fxDealDao;
    private final FxDealValidator fxDealValidator;


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public FxDeal importDeal(FxDealDTO dto) {
        FxDeal fxDeal = FxDeal.builder()
                .dealId(dto.getDealId())
                .currencyTo(dto.getCurrencyTo())
                .currencyFrom(dto.getCurrencyFrom())
                .dealAmount(dto.getDealAmount())
                .exchangeRate(dto.getExchangeRate())
                .build();

        if (fxDealDao.existsByFxDealId(dto.getDealId())) {
            log.warn("Duplicate deal detected: {}", dto.getDealId());
            throw new DuplicateDealException("Deal with ID '" + dto.getDealId() + "' already exists");
        }

        return fxDealDao.save(fxDeal);
    }
}
