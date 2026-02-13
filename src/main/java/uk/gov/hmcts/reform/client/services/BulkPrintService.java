package uk.gov.hmcts.reform.client.services;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGeneratorFactory;
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

    private final AuthTokenGenerator defaultAuthTokenGenerator;
    private final SendLetterApi sendLetterApi;
    private final ServiceAuthorisationApi serviceAuthorisationApi;

    private final Map<String, AuthTokenGenerator> authTokenGenerators = new HashMap<>();

    private AuthTokenGenerator tokenGeneratorForService(final String service, final String secret) {
        return authTokenGenerators.computeIfAbsent(service, s -> {
            return s == null || s.isBlank()
                ? defaultAuthTokenGenerator
                : AuthTokenGeneratorFactory.createDefaultGenerator(secret, s, serviceAuthorisationApi);
        });
    }

    /**
     * Creates the BulkPrint Request, loads the pdf to be sent from the resources folder. Then tries
     * to send it to Send Letter API.
     *
     * @param count             number of files to send
     * @param internationalPost If the letter to be sent is an international letter.
     * @param service           the service identifier
     * @param secret            the s2s secret
     * @param attrs             additional attributes to attach to the letter
     * @return list of UUIDs
     * @throws IOException - if there is an error reading from the pdf
     */
    public List<UUID> tryToSendMultiple(
        Long count,
        boolean internationalPost,
        final String service,
        final String secret,
        final Map<String, String> attrs) throws IOException {

        BulkPrintRequest bulkPrintRequest = buildBulkPrintRequest();

        ClassLoader classLoader = BulkPrintService.class.getClassLoader();
        File file = new File(Objects.requireNonNull(classLoader.getResource("test_pdf.pdf")).getFile());

        List<UUID> uuidList = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            try {
                UUID uuid = send(
                    bulkPrintRequest,
                    List.of(Files.readAllBytes(file.toPath())),
                    service,
                    secret,
                    getAdditionalData(internationalPost, attrs)
                );
                uuidList.add(uuid);
            } catch (FeignException | IOException ex) {
                log.warn("Error while sending pdf", ex);
                throw ex;
            }
        }

        return uuidList;
    }

    /**
     * Creates the BulkPrint Request, loads the pdf to be sent from the resources folder. Then tries
     * to send it to Send Letter API.
     * @param internationalPost If the letter to be sent is an international letter.
     * @return list of UUIDs
     * @throws IOException - if there is an error reading from the pdf
     */
    public List<UUID> tryToSend(boolean internationalPost) throws IOException {
        BulkPrintRequest bulkPrintRequest = buildBulkPrintRequest();

        ClassLoader classLoader = BulkPrintService.class.getClassLoader();
        File file = new File(Objects.requireNonNull(classLoader.getResource("test_pdf.pdf")).getFile());

        List<UUID> uuidList = new ArrayList<>();

        try {
            UUID uuid = send(
                bulkPrintRequest,
                List.of(Files.readAllBytes(file.toPath())),
                null,
                null,
                getAdditionalData(internationalPost, null)
            );
            uuidList.add(uuid);
            return uuidList;
        } catch (FeignException | IOException ex) {
            System.out.println(ex.getMessage());
            throw ex;
        }
    }

    /**
     * Gets authentication, creates a letter and sends it to Send Letter using the client service.
     * @param bulkPrintRequest - the letter data used by Send Letter API
     * @param listOfDocumentsAsByteArray - the files as a byte stream that should be printed
     * @return UUID that represents a successful call to Send Letter API
     */
    public UUID send(
        final BulkPrintRequest bulkPrintRequest,
        final List<byte[]> listOfDocumentsAsByteArray,
        String service,
        String secret,
        Map<String, Object> additionalData) {

        String s2sToken = tokenGeneratorForService(service, secret).generate();
        String caseId = bulkPrintRequest.getCaseId();

        log.info("Sending {} for case {}", bulkPrintRequest.getLetterType(), bulkPrintRequest.getCaseId());

        final List<Document> documents = listOfDocumentsAsByteArray.stream()
            .map(docBytes -> new Document(getEncoder().encodeToString(docBytes), 3))
            .collect(toList());

        SendLetterResponse sendLetterResponse = sendLetterApi.sendLetter(
            s2sToken,
            new LetterV3("a type",
                         documents,
                         additionalData
            )
        );

        log.info("Letter service produced the following letter Id {} for case {}", sendLetterResponse.letterId, caseId);
        return sendLetterResponse.letterId;
    }

    /**
     * Creates the Bulk Print document info used in the request.
     * @return BulkPrintRequest - info needed for sending a letter
     */
    public static BulkPrintRequest buildBulkPrintRequest() {
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

    /**
     * Gets the additional data for the request to Send Letter.
     * @return a map of data
     */
    private Map<String, Object> getAdditionalData(boolean internationalPost, Map<String, String> attrs) {
        final Map<String, Object> additionalData = new HashMap<>();

        additionalData.put(CASE_IDENTIFIER_KEY, "1448915163945522589");
        additionalData.put(CASE_REFERENCE_NUMBER_KEY, "1448915163945522588");
        additionalData.put("letterType", "general-letter");
        additionalData.put("isInternational", internationalPost);

        additionalData.put(RECIPIENTS, Arrays.asList("Gilligan Blobbers", "Querky Mcgibbins",
                                                     UUID.randomUUID(), UUID.randomUUID()));

        // add any additional attributes
        if (attrs != null) {
            additionalData.putAll(attrs);
        }

        return additionalData;
    }
}
