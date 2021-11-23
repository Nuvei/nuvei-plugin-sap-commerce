package com.nuvei.services.openorder.impl;

import com.nuvei.services.merchant.NuveiMerchantConfigurationService;
import com.nuvei.services.model.NuveiMerchantConfigurationModel;
import com.safecharge.exception.SafechargeException;
import com.safecharge.model.UserAddress;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.order.CartService;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class DefaultNuveiOpenOrderServiceTest {

    private static final String EMAIL = "email";
    private static final String CURRENCY_ISO = "EUR";
    private static final String CUSTOMER_ID = "customerId";
    private static final String CLIENT_UNIQUE_ID = "clientUniqueId";
    private static final String SESSION_TOKEN_ID = "SessionTokenID";
    private static final String PAYMENT_ADDRESS_CANT_BE_NULL = "PaymentAddress of session cart cartCode can't  be null";
    private static final String DELIVERY_ADDRESS_CANT_BE_NULL = "DeliveryAddress of session cart cartCode can't  be null";
    private static final String USER_UID_CAN_NEITHER_BE_NULL_NOR_EMPTY = "User uid of session cart cartCode can neither be null nor empty";
    private static final String CURRENCY_ISO_CAN_NEITHER_BE_NULL_NOR_EMPTY = "CurrencyIso of session cart cartCode can neither be null nor empty";
    private static final String CUSTOMER_CAN_NEITHER_BE_NULL_NOR_NOT_A_CUSTOMER = "Session cart: cartCode user can neither be null or not be a customer";
    private static final String CUSTOMER_CUSTOMERUID_CAN_NEITHER_BE_NULL_NOR_EMPTY = "Customer customerUid of session cart: cartCode can neither be null nor empty";

    private static final double TOTAL_PRICE = 100.0;
    private static final String CART_CODE = "cartCode";

    @Spy
    @InjectMocks
    private DefaultNuveiOpenOrderService testObj;

    @Mock
    private CartService cartServiceMock;
    @Mock
    private Converter<AddressModel, UserAddress> userAddressConverterMock;
    @Mock
    private NuveiMerchantConfigurationService nuveiMerchantConfigurationServiceMock;

    @Mock
    private UserModel userModelMock;
    @Mock
    private CartModel cartModelMock;
    @Mock
    private UserAddress billingAddressMock, shippingAddressMock;
    @Mock
    private CurrencyModel currencyModelMock;
    @Mock
    private AddressModel paymentAddressMock, deliveryAddressMock;
    @Mock
    private CustomerModel customerModelMock;
    @Mock
    private NuveiMerchantConfigurationModel currentConfigurationMock;

    @Before
    public void setUp() {
        when(cartModelMock.getCode()).thenReturn(CART_CODE);
        when(cartServiceMock.getSessionCart()).thenReturn(cartModelMock);
        when(cartModelMock.getPaymentAddress()).thenReturn(paymentAddressMock);
        when(cartModelMock.getDeliveryAddress()).thenReturn(deliveryAddressMock);
        when(nuveiMerchantConfigurationServiceMock.getCurrentConfiguration()).thenReturn(currentConfigurationMock);
        when(userAddressConverterMock.convert(paymentAddressMock)).thenReturn(billingAddressMock);
        when(userAddressConverterMock.convert(deliveryAddressMock)).thenReturn(shippingAddressMock);
        when(cartModelMock.getUser()).thenReturn(customerModelMock);
        when(cartModelMock.getCurrency()).thenReturn(currencyModelMock);
        when(cartModelMock.getTotalPrice()).thenReturn(TOTAL_PRICE);
        when(cartModelMock.getClientUniqueId()).thenReturn(CLIENT_UNIQUE_ID);
        when(currencyModelMock.getIsocode()).thenReturn(CURRENCY_ISO);
        when(customerModelMock.getCustomerID()).thenReturn(CUSTOMER_ID);
        when(customerModelMock.getUid()).thenReturn(EMAIL);
    }

    @Test
    public void requestOpenOrder_ShouldReturnSessionTokenID_WhenTheRequestIsSuccessful() throws SafechargeException {
        doReturn(SESSION_TOKEN_ID).when(testObj)
                .executeRequest(CURRENCY_ISO, CUSTOMER_ID,
                        String.valueOf(TOTAL_PRICE), currentConfigurationMock, billingAddressMock, shippingAddressMock, CLIENT_UNIQUE_ID);

        final String result = testObj.requestOpenOrder();

        assertThat(result).isEqualTo(SESSION_TOKEN_ID);
    }

    @Test(expected = SafechargeException.class)
    public void requestOpenOrder_ShouldThrowException_WhenTheRequestIsNotSuccessful() throws SafechargeException {
        doThrow(SafechargeException.class).when(testObj)
                .executeRequest(CURRENCY_ISO, CUSTOMER_ID,
                        String.valueOf(TOTAL_PRICE), currentConfigurationMock, billingAddressMock, shippingAddressMock, CLIENT_UNIQUE_ID);

        testObj.requestOpenOrder();
    }

    @Test
    public void requestOpenOrder_ShouldThrowException_WhenTheUserIsNull() {
        when(cartModelMock.getUser()).thenReturn(null);

        assertThatThrownBy(() -> testObj.requestOpenOrder())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(CUSTOMER_CAN_NEITHER_BE_NULL_NOR_NOT_A_CUSTOMER);
    }

    @Test
    public void requestOpenOrder_ShouldThrowException_WhenTheUserIsNotACustomer() {
        when(cartModelMock.getUser()).thenReturn(userModelMock);

        assertThatThrownBy(() -> testObj.requestOpenOrder())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(CUSTOMER_CAN_NEITHER_BE_NULL_NOR_NOT_A_CUSTOMER);
    }

    @Test
    public void requestOpenOrder_ShouldThrowException_WhenTheUserUidIsEmpty() {
        when(cartModelMock.getUser()).thenReturn(customerModelMock);
        when(customerModelMock.getUid()).thenReturn(StringUtils.EMPTY);

        assertThatThrownBy(() -> testObj.requestOpenOrder())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(USER_UID_CAN_NEITHER_BE_NULL_NOR_EMPTY);
    }

    @Test
    public void requestOpenOrder_ShouldThrowException_WhenTheUserUidIsNull() {
        when(cartModelMock.getUser()).thenReturn(customerModelMock);
        when(customerModelMock.getUid()).thenReturn(null);

        assertThatThrownBy(() -> testObj.requestOpenOrder())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(USER_UID_CAN_NEITHER_BE_NULL_NOR_EMPTY);
    }

    @Test
    public void requestOpenOrder_ShouldThrowException_WhenTheCustomerUidIsEmpty() {
        when(cartModelMock.getUser()).thenReturn(customerModelMock);
        when(customerModelMock.getCustomerID()).thenReturn(StringUtils.EMPTY);

        assertThatThrownBy(() -> testObj.requestOpenOrder())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(CUSTOMER_CUSTOMERUID_CAN_NEITHER_BE_NULL_NOR_EMPTY);
    }

    @Test
    public void requestOpenOrder_ShouldThrowException_WhenTheCustomerUidIsNull() {
        when(cartModelMock.getUser()).thenReturn(customerModelMock);
        when(customerModelMock.getCustomerID()).thenReturn(null);

        assertThatThrownBy(() -> testObj.requestOpenOrder())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(CUSTOMER_CUSTOMERUID_CAN_NEITHER_BE_NULL_NOR_EMPTY);
    }

    @Test
    public void requestOpenOrder_ShouldThrowException_WhenTheCartHasNoCurrency() {
        when(cartModelMock.getCurrency()).thenReturn(null);

        assertThatThrownBy(() -> testObj.requestOpenOrder())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(CURRENCY_ISO_CAN_NEITHER_BE_NULL_NOR_EMPTY);
    }

    @Test
    public void requestOpenOrder_ShouldThrowException_WhenTheCartCurrencyHasNoIsoCode() {
        when(cartModelMock.getCurrency()).thenReturn(currencyModelMock);
        when(currencyModelMock.getIsocode()).thenReturn(null);

        assertThatThrownBy(() -> testObj.requestOpenOrder())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(CURRENCY_ISO_CAN_NEITHER_BE_NULL_NOR_EMPTY);
    }

    @Test
    public void requestOpenOrder_ShouldThrowException_WhenTheCartCurrencyIsoCodeIsEmpty() {
        when(cartModelMock.getCurrency()).thenReturn(currencyModelMock);
        when(currencyModelMock.getIsocode()).thenReturn(StringUtils.EMPTY);

        assertThatThrownBy(() -> testObj.requestOpenOrder())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(CURRENCY_ISO_CAN_NEITHER_BE_NULL_NOR_EMPTY);
    }

    @Test
    public void requestOpenOrder_ShouldThrowException_WhenTheCartHasNoBillingAddress() {
        when(cartModelMock.getPaymentAddress()).thenReturn(null);

        assertThatThrownBy(() -> testObj.requestOpenOrder())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(PAYMENT_ADDRESS_CANT_BE_NULL);
    }

    @Test
    public void requestOpenOrder_ShouldThrowException_WhenTheCartHasNoDeliveryAddress() {
        when(cartModelMock.getDeliveryAddress()).thenReturn(null);

        assertThatThrownBy(() -> testObj.requestOpenOrder())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(DELIVERY_ADDRESS_CANT_BE_NULL);
    }
}
