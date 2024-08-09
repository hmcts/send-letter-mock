package uk.gov.hmcts.reform.client.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import uk.gov.hmcts.reform.client.services.BulkPrintService;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(
    path = "/",
    produces = {MediaType.APPLICATION_JSON_VALUE}
)
public class MockSendController {

    private final BulkPrintService bulkPrintService;

    @Autowired
    public MockSendController(BulkPrintService bulkPrintService) {
        this.bulkPrintService = bulkPrintService;
    }

    @GetMapping("/test")
    public ResponseEntity<List<UUID>> test() throws IOException {
        return ResponseEntity.ok(bulkPrintService.tryToSend(false));
    }

    @GetMapping("/test-international-post")
    public ResponseEntity<List<UUID>> testInternationalPost() throws IOException {
        return ResponseEntity.ok(bulkPrintService.tryToSend(true));
    }
}
