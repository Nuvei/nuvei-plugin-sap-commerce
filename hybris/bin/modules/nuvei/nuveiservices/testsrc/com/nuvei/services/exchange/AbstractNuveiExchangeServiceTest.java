package com.nuvei.services.exchange;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.nuvei.services.enums.NuveiHashAlgorithm;
import com.nuvei.services.model.NuveiMerchantConfigurationModel;
import com.nuvei.services.model.NuveiPayloadModel;
import com.nuvei.services.service.NuveiPaymentTransactionService;
import com.safecharge.request.SettleTransactionRequest;
import com.safecharge.response.SafechargeResponse;
import com.safecharge.response.SettleTransactionResponse;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.servicelayer.model.ModelService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class AbstractNuveiExchangeServiceTest {

    private static final String HOST = "host";
    private static final String SECRET_KEY = "secretKey";
    private static final String SITE = "site";
    private static final String MERCHANT_ID = "merchantId";
    private static final String SESSION_TOKEN = "sessionToken";

    @Spy
    @InjectMocks
    private MyAbstractNuveiExchangeService testObj;
    @Mock
    private NuveiMerchantConfigurationModel nuveiMerchantConfigurationModelMock;
    @Mock
    private ModelService modelServiceMock;
    @Mock
    private NuveiPaymentTransactionService nuveiPaymentTransactionService;
    @Mock
    private SafechargeResponse getSessionTokenResponseMock;
    @Mock
    private NuveiPayloadModel nuveiPayloadModelMock, orderPayloadModelMock;

    @Before
    public void setUp() throws Exception {
        when(nuveiMerchantConfigurationModelMock.getHashAlgorithm()).thenReturn(NuveiHashAlgorithm.SHA256);
        when(nuveiMerchantConfigurationModelMock.getMerchantId()).thenReturn(MERCHANT_ID);
        when(nuveiMerchantConfigurationModelMock.getMerchantSiteId()).thenReturn(SITE);
        when(nuveiMerchantConfigurationModelMock.getMerchantSecretKey()).thenReturn(SECRET_KEY);
        when(nuveiMerchantConfigurationModelMock.getServerHost()).thenReturn(HOST);
        when(getSessionTokenResponseMock.getSessionToken()).thenReturn(SESSION_TOKEN);
    }

    @Test
    public void storeSafechargeRequest_ShouldstoreSafechargeRequest_WhenRequestCanBeParsed() {
        final SettleTransactionRequest settleTransactionRequestStub = new SettleTransactionRequest();
        final OrderModel orderModelStub = new OrderModel();

        when(modelServiceMock.create(NuveiPayloadModel.class)).thenReturn(nuveiPayloadModelMock);
        orderModelStub.setRequestsPayload(List.of(orderPayloadModelMock));

        testObj.storeSafechargeRequest(orderModelStub, settleTransactionRequestStub);

        assertThat(orderModelStub.getRequestsPayload()).containsExactly(orderPayloadModelMock, nuveiPayloadModelMock);
    }

    @Test
    public void storeSafechargeRequest_ShouldThrowExceptionAndNotModifyOrderRequestsPayloads_WhenRequestCanNotBeParsed() {
        final SettleTransactionRequest settleTransactionRequestStub = new SettleTransactionRequest();
        final OrderModel orderModelStub = new OrderModel();

        when(modelServiceMock.create(NuveiPayloadModel.class)).thenReturn(nuveiPayloadModelMock);
        doThrow(JsonProcessingException.class).when(nuveiPayloadModelMock).setPayload(anyString());
        orderModelStub.setRequestsPayload(List.of(orderPayloadModelMock));

        testObj.storeSafechargeRequest(orderModelStub, settleTransactionRequestStub);

        assertThat(orderModelStub.getRequestsPayload()).containsExactly(orderPayloadModelMock);
    }

    @Test
    public void storeSafechargeResponse_ShouldstoreSafechargeResponse_WhenResponseCanBeParsed() {
        final SettleTransactionResponse settleTransactionResponseStub = new SettleTransactionResponse();
        final OrderModel orderModelStub = new OrderModel();

        when(modelServiceMock.create(NuveiPayloadModel.class)).thenReturn(nuveiPayloadModelMock);
        orderModelStub.setResponsesPayload(List.of(orderPayloadModelMock));

        testObj.storeSafechargeResponse(orderModelStub, settleTransactionResponseStub);

        assertThat(orderModelStub.getResponsesPayload()).containsExactly(orderPayloadModelMock, nuveiPayloadModelMock);
    }

    @Test
    public void storeSafechargeResponse_ShouldThrowExceptionAndNotModifyOrderResponsesPayloads_WhenResponseCanNotBeParsed() {
        final SettleTransactionResponse settleTransactionResponseStub = new SettleTransactionResponse();
        final OrderModel orderModelStub = new OrderModel();

        when(modelServiceMock.create(NuveiPayloadModel.class)).thenReturn(nuveiPayloadModelMock);
        doThrow(JsonProcessingException.class).when(nuveiPayloadModelMock).setPayload(anyString());
        orderModelStub.setResponsesPayload(List.of(orderPayloadModelMock));

        testObj.storeSafechargeResponse(orderModelStub, settleTransactionResponseStub);

        assertThat(orderModelStub.getResponsesPayload()).containsExactly(orderPayloadModelMock);
    }


    public static class MyAbstractNuveiExchangeService extends AbstractNuveiExchangeService {
        public MyAbstractNuveiExchangeService(final ModelService modelService, final NuveiPaymentTransactionService nuveiPaymentTransactionService) {
            super(modelService, nuveiPaymentTransactionService);
        }
    }


}
