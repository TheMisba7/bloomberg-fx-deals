package org.boolmberg.datawarehouse.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.boolmberg.datawarehouse.app.FxDealApp;
import org.boolmberg.datawarehouse.dto.FxDealDTO;
import org.boolmberg.datawarehouse.dto.ImportSummary;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/fx-deals")
@RestController
public class FxDealApi {

    private final FxDealApp fxDealApp;

    @GetMapping("/{dealId}")
    @ResponseStatus(HttpStatus.OK)
    public FxDealDTO getFxDealById(@PathVariable String dealId) {
        return fxDealApp.getFxDealById(dealId);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<FxDealDTO> getAllFxDeals() {
        return fxDealApp.findAllDeals();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ImportSummary importDeal(@Valid @RequestBody FxDealDTO dto) {
        return fxDealApp.importDeal(dto);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(path = "/upload", consumes = {"multipart/form-data"})
    public ImportSummary uploadCSV(@RequestParam("file") MultipartFile file) {
        return fxDealApp.uploadCsv(file);
    }
}
