package uk.gov.hmcts.reform.rsecheck.services;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import uk.gov.hmcts.reform.client.services.BulkPrintService;
import uk.gov.hmcts.reform.sendletter.api.SendLetterApi;
import uk.gov.hmcts.reform.sendletter.api.SendLetterResponse;
import uk.gov.hmcts.reform.sendletter.api.model.v3.LetterV3;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = BulkPrintService.class)
public class BulkPrintServiceTest {

    @Autowired
    BulkPrintService bulkPrintService;

    @MockBean
    SendLetterApi sendLetterApi;

    @MockBean
    AuthTokenGenerator authTokenGenerator;

    @Test
    void shouldReturnUuidAfterSendingLetter() throws IOException {
        UUID uuid = UUID.randomUUID();
        SendLetterResponse sendLetterResponse = new SendLetterResponse(uuid);

        when(sendLetterApi.sendLetter(any(), any(LetterV3.class))).thenReturn(sendLetterResponse);

        List<UUID> actualUuid = bulkPrintService.tryToSend(true);

        assertThat(actualUuid).isEqualTo(Lists.list(uuid));
    }

}
