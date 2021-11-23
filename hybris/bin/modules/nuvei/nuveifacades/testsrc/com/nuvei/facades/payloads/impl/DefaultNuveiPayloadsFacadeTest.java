package com.nuvei.facades.payloads.impl;

import com.nuvei.services.payloads.NuveiPayloadsService;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.order.CartService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.*;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class DefaultNuveiPayloadsFacadeTest {

    private static final String REQUEST = "request";
    private static final String RESPONSE = "response";

    @InjectMocks
    private DefaultNuveiPayloadsFacade testObj;

    @Mock
    private NuveiPayloadsService nuveiPayloadsServiceMock;
    @Mock
    private CartService cartServiceMock;

    @Mock
    private CartModel cartModelMock;

    @Test
    public void setPaymentRequestPayload_ShouldSetPaymentRequestToCart_WhenHasSessionCart() {
        when(cartServiceMock.hasSessionCart()).thenReturn(Boolean.TRUE);
        when(cartServiceMock.getSessionCart()).thenReturn(cartModelMock);

        testObj.setPaymentRequestPayload(REQUEST);

        verify(nuveiPayloadsServiceMock).setPaymentRequestPayload(REQUEST, cartModelMock);
    }

    @Test
    public void setPaymentResponsePayload_ShouldSetPaymentResponseToCart_WhenHasSessionCart() {
        when(cartServiceMock.hasSessionCart()).thenReturn(Boolean.TRUE);
        when(cartServiceMock.getSessionCart()).thenReturn(cartModelMock);

        testObj.setPaymentResponsePayload(RESPONSE);

        verify(nuveiPayloadsServiceMock).setPaymentResponsePayload(RESPONSE, cartModelMock);
    }

    @Test
    public void setPaymentRequestPayload_ShoulDoNothing_WhenHasNoSessionCart() {
        when(cartServiceMock.hasSessionCart()).thenReturn(Boolean.FALSE);

        testObj.setPaymentRequestPayload(REQUEST);

        verifyZeroInteractions(nuveiPayloadsServiceMock);
    }
}
