package com.nuvei.services.payloads.impl;

import com.nuvei.services.model.NuveiPayloadModel;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.servicelayer.model.ModelService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class DefaultNuveiPayloadsServiceTest {

    private static final String REQUEST = "request";
    private static final String RESPONSE = "response";

    @InjectMocks
    private DefaultNuveiPayloadsService testObj;

    @Mock
    private ModelService modelServiceMock;

    private final AbstractOrderModel abstractOrderModel = new OrderModel();
    private final NuveiPayloadModel nuveiPayloadModel = new NuveiPayloadModel();

    @Test
    public void setPaymentRequestPayload_ShouldCreateRequestPayloadAndSetIntoTheOrder() {
        abstractOrderModel.setRequestsPayload(new ArrayList<>());

        when(modelServiceMock.create(NuveiPayloadModel.class)).thenReturn(nuveiPayloadModel);

        testObj.setPaymentRequestPayload(REQUEST, abstractOrderModel);

        assertThat(nuveiPayloadModel.getPayload()).isEqualTo(REQUEST);
        verify(modelServiceMock).save(nuveiPayloadModel);
        assertThat(nuveiPayloadModel.getRequestOrder()).isEqualTo(abstractOrderModel);
    }

    @Test
    public void setPaymentResponsePayload_ShouldCreateResponsePayloadAndSetIntoTheOrder() {
        abstractOrderModel.setResponsesPayload(new ArrayList<>());

        when(modelServiceMock.create(NuveiPayloadModel.class)).thenReturn(nuveiPayloadModel);

        testObj.setPaymentResponsePayload(RESPONSE, abstractOrderModel);

        assertThat(nuveiPayloadModel.getPayload()).isEqualTo(RESPONSE);
        verify(modelServiceMock).save(nuveiPayloadModel);
        assertThat(nuveiPayloadModel.getResponseOrder()).isEqualTo(abstractOrderModel);
    }


    @Test(expected = IllegalArgumentException.class)
    public void setPaymentRequestPayload_ShouldThrowException_IfRequestIsNull() {
        testObj.setPaymentRequestPayload(null, abstractOrderModel);
    }

    @Test(expected = IllegalArgumentException.class)
    public void setPaymentRequestPayload_ShouldThrowException_IfOrdertIsNull() {
        testObj.setPaymentRequestPayload(REQUEST, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void setPaymentResponsePayload_ShouldThrowException_IfResponseIsNull() {
        testObj.setPaymentResponsePayload(null, abstractOrderModel);
    }

    @Test(expected = IllegalArgumentException.class)
    public void setPaymentResponsePayload_ShouldThrowException_IfOrdertIsNull() {
        testObj.setPaymentResponsePayload(RESPONSE, null);
    }

}
