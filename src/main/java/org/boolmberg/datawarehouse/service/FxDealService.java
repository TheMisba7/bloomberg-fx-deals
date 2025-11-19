package org.boolmberg.datawarehouse.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.boolmberg.datawarehouse.dao.FxDealDao;
import org.boolmberg.datawarehouse.dto.FxDealDTO;
import org.boolmberg.datawarehouse.exception.DuplicateDealException;
import org.boolmberg.datawarehouse.model.FxDeal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FxDealService {

    private final FxDealDao fxDealDao;


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public FxDeal importDeal(FxDealDTO dto) {
        FxDeal fxDeal = FxDeal.builder()
                .dealId(dto.getDealId())
                .currencyTo(dto.getCurrencyTo())
                .currencyFrom(dto.getCurrencyFrom())
                .dealAmount(dto.getDealAmount())
                .exchangeRate(dto.getExchangeRate())
                .build();

        if (fxDealDao.existsByDealId(dto.getDealId())) {
            log.warn("Duplicate deal detected: {}", dto.getDealId());
            throw new DuplicateDealException("Deal with ID '" + dto.getDealId() + "' already exists");
        }

        return fxDealDao.save(fxDeal);
    }
}
