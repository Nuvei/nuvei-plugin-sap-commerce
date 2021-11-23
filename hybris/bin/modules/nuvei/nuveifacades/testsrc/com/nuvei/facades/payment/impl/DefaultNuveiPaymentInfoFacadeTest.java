package com.nuvei.facades.payment.impl;

import com.nuvei.facades.beans.NuveiSDKResponseData;
import com.nuvei.services.model.NuveiPaymentInfoModel;
import com.nuvei.services.payments.NuveiPaymentInfoService;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.order.payment.PaymentInfoModel;
import de.hybris.platform.order.CartService;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.*;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class DefaultNuveiPaymentInfoFacadeTest {

    @InjectMocks
    private DefaultNuveiPaymentInfoFacade testObj;

    @Mock
    private CartService cartServiceMock;
    @Mock
    private NuveiPaymentInfoService nuveiPaymentInfoServiceMock;
    @Mock
    private Converter<NuveiSDKResponseData, NuveiPaymentInfoModel> nuveiPaymentInfoModelConverterMock;

    private final NuveiSDKResponseData nuveiSDKResponseData = new NuveiSDKResponseData();
    private final PaymentInfoModel paymentInfoModel = new PaymentInfoModel();
    private final NuveiPaymentInfoModel nuveiPaymentInfoModel = new NuveiPaymentInfoModel();
    private final CartModel cartModel = new CartModel();

    @Test
    public void addPaymentInfoToCart_ShouldAddPaymentInfoToCart_WhenHasSessionCart() {
        when(cartServiceMock.hasSessionCart()).thenReturn(Boolean.TRUE);
        when(cartServiceMock.getSessionCart()).thenReturn(cartModel);
        when(nuveiPaymentInfoModelConverterMock.convert(nuveiSDKResponseData)).thenReturn(nuveiPaymentInfoModel);

        testObj.addPaymentInfoToCart(nuveiSDKResponseData);

        verify(nuveiPaymentInfoServiceMock).createPaymentInfo(nuveiPaymentInfoModel, cartModel);
        verify(nuveiPaymentInfoServiceMock, never()).removePaymentInfo(any());
    }

    @Test
    public void addPaymentInfoToCart_ShouldDoNothing_WhenHasNoSessionCart() {
        when(cartServiceMock.hasSessionCart()).thenReturn(Boolean.FALSE);

        testObj.addPaymentInfoToCart(nuveiSDKResponseData);

        verifyZeroInteractions(nuveiPaymentInfoServiceMock);
    }

    @Test
    public void addPaymentInfoToCart_ShouldRemovePreviosPaymentInfo_WhenHasASessionCartWithAPaymentInfoSet() {
        cartModel.setPaymentInfo(paymentInfoModel);
        
        when(cartServiceMock.hasSessionCart()).thenReturn(Boolean.TRUE);
        when(cartServiceMock.getSessionCart()).thenReturn(cartModel);
        when(nuveiPaymentInfoModelConverterMock.convert(nuveiSDKResponseData)).thenReturn(nuveiPaymentInfoModel);

        testObj.addPaymentInfoToCart(nuveiSDKResponseData);

        verify(nuveiPaymentInfoServiceMock).removePaymentInfo(cartModel);
        verify(nuveiPaymentInfoServiceMock).createPaymentInfo(nuveiPaymentInfoModel, cartModel);
    }
}
