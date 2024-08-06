package uk.gov.hmcts.reform.client.services;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.client.models.BulkPrintDocument;
import uk.gov.hmcts.reform.client.models.BulkPrintRequest;
import uk.gov.hmcts.reform.sendletter.api.SendLetterApi;
import uk.gov.hmcts.reform.sendletter.api.SendLetterResponse;
import uk.gov.hmcts.reform.sendletter.api.model.v3.Document;
import uk.gov.hmcts.reform.sendletter.api.model.v3.LetterV3;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static java.util.Base64.getEncoder;
import static java.util.stream.Collectors.toList;

/**
 * Note: the order of documents you send to this service is the order in which they will print.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BulkPrintService {

    private static final String CASE_REFERENCE_NUMBER_KEY = "caseReferenceNumber";
    private static final String CASE_IDENTIFIER_KEY = "caseIdentifier";
    private static final String RECIPIENTS = "recipients";

    private final AuthTokenGenerator authTokenGenerator;
    private final SendLetterApi sendLetterApi;

    /**
     * Gets authentication, creates a letter and sends it to Send Letter using the client service.
     * @param bulkPrintRequest - the letter data used by Send Letter API
     * @param listOfDocumentsAsByteArray - the files as a byte stream that should be printed
     * @return UUID that represents a successful call to Send Letter API
     */
    public UUID send(final BulkPrintRequest bulkPrintRequest, final List<byte[]> listOfDocumentsAsByteArray) {

        String s2sToken = authTokenGenerator.generate();
        String caseId = bulkPrintRequest.getCaseId();

        log.info("Sending {} for case {}", bulkPrintRequest.getLetterType(), bulkPrintRequest.getCaseId());

        final List<Document> documents = listOfDocumentsAsByteArray.stream()
            .map(docBytes -> new Document(getEncoder().encodeToString(docBytes), 3))
            .collect(toList());

        SendLetterResponse sendLetterResponse = sendLetterApi.sendLetter(
            s2sToken,
            new LetterV3("a type",
                         documents,
                         getAdditionalData()
            )
        );

        log.info("Letter service produced the following letter Id {} for case {}", sendLetterResponse.letterId, caseId);
        return sendLetterResponse.letterId;
    }

    /**
     * Gets the additional data for the request to Send Letter.
     * @return a map of data
     */
    @SuppressWarnings({"PMD.ExcessiveParameterList"})
    private Map<String, Object> getAdditionalData() {
        final Map<String, Object> additionalData = new HashMap<>();

        additionalData.put(CASE_IDENTIFIER_KEY, "1448915163945522589");
        additionalData.put(CASE_REFERENCE_NUMBER_KEY, "1448915163945522588");
        additionalData.put("letterType", "general-letter");
        additionalData.put("isInternational", true);

        additionalData.put(RECIPIENTS, Arrays.asList("Gilligan Blobbers", "Querky Mcgibbins",
                                                     UUID.randomUUID(), UUID.randomUUID()));

        return additionalData;
    }

    /**
     * Creates the BulkPrint Request, loads the pdf to be sent from the resources folder. Then tries
     * to send to Send Letter API.
     * @return list of UUIDs
     * @throws IOException - if there is an error reading from the pdf
     */
    public List<UUID> tryToSend() throws IOException {
        BulkPrintRequest bulkPrintRequest = buildBulkPrintRequest();

        ClassLoader classLoader = BulkPrintService.class.getClassLoader();
        File file = new File(Objects.requireNonNull(classLoader.getResource("test_pdf.pdf")).getFile());

        List<UUID> uuidList = new ArrayList<>();

        try {
            for (int i = 0; i < 1; i++) {

                UUID uuid = send(
                    bulkPrintRequest,
                    List.of(
                        Files.readAllBytes(file.toPath())
                    )
                );
                uuidList.add(uuid);
            }
            return uuidList;
        } catch (FeignException | IOException ex) {
            System.out.println(ex.getMessage());
            throw ex;
        }
    }

    private static BulkPrintRequest buildBulkPrintRequest() {
        return getBulkPrintDocuments();
    }

    /**
     * Creates the Bulk Print document info used in the request.
     * @return BulkPrintRequest - info needed for sending a letter
     */
    public static BulkPrintRequest getBulkPrintDocuments() {
        BulkPrintDocument bulkPrintDocument = new BulkPrintDocument();
        bulkPrintDocument.setFileName("dummy.pdf");
        bulkPrintDocument.setBinaryFileUrl("dummy pdf url");
        BulkPrintDocument bulkPrintDocument2 = new BulkPrintDocument();
        bulkPrintDocument2.setFileName("dummy.pdf2");
        bulkPrintDocument2.setBinaryFileUrl("dummy pdf url2");

        BulkPrintRequest bulkPrintRequest = new BulkPrintRequest();
        bulkPrintRequest.setBulkPrintDocuments(Arrays.asList(bulkPrintDocument, bulkPrintDocument2));
        bulkPrintRequest.setCaseId("caseid");
        bulkPrintRequest.setLetterType("caselettertype");
        return bulkPrintRequest;
    }
}
