package org.boolmberg.datawarehouse.aspect;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.boolmberg.datawarehouse.dto.FxDealDTO;
import org.boolmberg.datawarehouse.model.FxDeal;
import org.boolmberg.datawarehouse.validator.FxDealValidator;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class FxDealImportAspect {

    private final FxDealValidator validator;

    @Around("execution(* org.boolmberg.datawarehouse.service.FxDealService.importDeal(..))")
    public Object aroundImportDeal(ProceedingJoinPoint joinPoint, FxDealDTO dealDto) throws Exception {
        log.info("Starting import: {}", dealDto.getDealId());
        validator.validate(dealDto);

        try {
            FxDeal result = (FxDeal) joinPoint.proceed();
            log.info("Successfully imported FX deal: {}", dealDto.getDealId());
            return result;

        } catch (Exception e) {
            log.error("Failed: {}", dealDto.getDealId());
            throw e;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
