package com.leadmapspro.api;

import com.leadmapspro.api.dto.ExtractRequest;
import com.leadmapspro.api.dto.ExtractResponse;
import com.leadmapspro.service.LeadExtractService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/extract")
@CrossOrigin(origins = "${leadmaps.cors.allowed-origins:http://localhost:4200}")
public class ExtractController {

    private final LeadExtractService leadExtractService;

    public ExtractController(LeadExtractService leadExtractService) {
        this.leadExtractService = leadExtractService;
    }

    @PostMapping
    public ExtractResponse extract(@Valid @RequestBody ExtractRequest request) {
        return leadExtractService.extract(request);
    }
}
