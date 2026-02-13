package uk.gov.hmcts.reform.client.controllers;

import uk.gov.hmcts.reform.client.services.BulkPrintService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping(
    path = "/",
    produces = {MediaType.APPLICATION_JSON_VALUE}
)
@RequiredArgsConstructor
@Slf4j
public class MockSendController {

    private final BulkPrintService bulkPrintService;

    @GetMapping("/test")
    public ResponseEntity<List<UUID>> test() throws IOException {
        return ResponseEntity.ok(bulkPrintService.tryToSend(false));
    }

    @GetMapping("/test-international-post")
    public ResponseEntity<List<UUID>> testInternationalPost() throws IOException {
        return ResponseEntity.ok(bulkPrintService.tryToSend(true));
    }

    @GetMapping("/test-multiple")
    public ResponseEntity<List<UUID>> testMultiple(
        @RequestParam(value = "count") Long count,
        @RequestParam(value = "international", required = false, defaultValue = "false") Boolean internationalPost,
        @RequestParam(value = "service", required = false) String service,
        @RequestParam(value = "s2s-key", required = false) String secret,
        @RequestParam(value = "attrs", required = false) Map<String, String> attrs)
        throws IOException {

        return ResponseEntity.ok(
            bulkPrintService.tryToSendMultiple(count, internationalPost, service, secret, attrs)
        );
    }
}
