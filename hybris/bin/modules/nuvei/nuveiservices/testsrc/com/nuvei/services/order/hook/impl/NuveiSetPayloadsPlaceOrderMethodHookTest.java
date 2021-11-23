package com.nuvei.services.order.hook.impl;

import com.nuvei.services.model.NuveiPayloadModel;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.commerceservices.service.data.CommerceCheckoutParameter;
import de.hybris.platform.commerceservices.service.data.CommerceOrderResult;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.order.InvalidCartException;
import de.hybris.platform.servicelayer.model.ModelService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class NuveiSetPayloadsPlaceOrderMethodHookTest {

    @InjectMocks
    private NuveiSetPayloadsPlaceOrderMethodHook testObj;

    @Mock
    private ModelService modelServiceMock;

    private final CartModel cartModel = new CartModel();
    private final OrderModel orderModel = new OrderModel();
    private final CommerceCheckoutParameter parameter = new CommerceCheckoutParameter();
    private final CommerceOrderResult commerceOrderResult = new CommerceOrderResult();
    private final NuveiPayloadModel firstRequestPayload = new NuveiPayloadModel();
    private final NuveiPayloadModel secondRequestPayload = new NuveiPayloadModel();
    private final NuveiPayloadModel firstResponsePayload = new NuveiPayloadModel();
    private final NuveiPayloadModel secondResponsePayload = new NuveiPayloadModel();

    @Test
    public void afterPlaceOrder_ShouldSetRequestsAndResponsesFromCartToOrder() throws InvalidCartException {
        parameter.setCart(cartModel);
        commerceOrderResult.setOrder(orderModel);
        cartModel.setRequestsPayload(List.of(firstRequestPayload, secondRequestPayload));
        cartModel.setResponsesPayload(List.of(firstResponsePayload, secondResponsePayload));

        testObj.afterPlaceOrder(parameter, commerceOrderResult);

        assertThat(orderModel.getRequestsPayload()).isEqualTo(List.of(firstRequestPayload, secondRequestPayload));
        assertThat(orderModel.getResponsesPayload()).isEqualTo(List.of(firstResponsePayload, secondResponsePayload));
        verify(modelServiceMock).save(orderModel);
    }

    @Test(expected = IllegalArgumentException.class)
    public void afterPlaceOrder_ShouldThrownException_WhenParameterCartIsNull() throws InvalidCartException {
        commerceOrderResult.setOrder(orderModel);

        testObj.afterPlaceOrder(parameter, commerceOrderResult);
    }

    @Test(expected = IllegalArgumentException.class)
    public void afterPlaceOrder_ShouldThrownException_WhenCommerceOrderResultOrderIsNull() throws InvalidCartException {
        parameter.setCart(cartModel);

        testObj.afterPlaceOrder(parameter, commerceOrderResult);
    }
}
