package uk.gov.hmcts.reform.client.models;

import lombok.Data;

/**
 * Model that represents a Bulk Print document. Used for building a Bulk Print Request.
 */
@Data
public class BulkPrintDocument {

    private String binaryFileUrl;
    private String fileName;
}
