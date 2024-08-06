package uk.gov.hmcts.reform.client.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
/**
 * Model that represents a Bulk Print document. Used for building a Bulk Print Request.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@EqualsAndHashCode
public class BulkPrintDocument {

    private String binaryFileUrl;
    private String fileName;
}
