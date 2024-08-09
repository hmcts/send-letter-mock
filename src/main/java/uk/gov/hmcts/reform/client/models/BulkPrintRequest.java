package uk.gov.hmcts.reform.client.models;

import lombok.Data;

import java.util.List;

/**
 * Model that represents the information that is sent along with the letter to Bulk Print.
 */
@Data
public class BulkPrintRequest {

    private String caseId;
    private String letterType;
    private List<BulkPrintDocument> bulkPrintDocuments;
}
