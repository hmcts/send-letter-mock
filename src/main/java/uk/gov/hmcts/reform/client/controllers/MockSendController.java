package uk.gov.hmcts.reform.client.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import uk.gov.hmcts.reform.client.services.BulkPrintService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import java.util.List;
import java.util.Objects;
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

    @GetMapping("/get-byte-array")
    public ResponseEntity<byte[]> getByteArray() throws IOException {
        ClassLoader classLoader = MockSendController.class.getClassLoader();
        File file = new File(Objects.requireNonNull(classLoader.getResource("test_pdf.pdf")).getFile());
        return ResponseEntity.ok(Files.readAllBytes(file.toPath()));
    }

    @GetMapping("/test")
    public ResponseEntity<List<UUID>> test() throws IOException {
        return ResponseEntity.ok(bulkPrintService.tryToSend());
    }
}
