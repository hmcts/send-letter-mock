package uk.gov.hmcts.reform.client.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;
/**
 * Model that represents the information that is sent along with the letter to Bulk Print.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@EqualsAndHashCode
public class BulkPrintRequest {

    private String caseId;
    private String letterType;
    private List<BulkPrintDocument> bulkPrintDocuments;
}
