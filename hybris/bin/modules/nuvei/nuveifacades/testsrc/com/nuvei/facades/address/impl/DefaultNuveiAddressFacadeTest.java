package com.nuvei.facades.address.impl;

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.commerceservices.delivery.DeliveryService;
import de.hybris.platform.core.PK;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.order.CartService;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.model.ModelService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class DefaultNuveiAddressFacadeTest {

    private static final PK ADDRESS_PK = PK.fromLong(1234L);

    private static final String ADDRESS_ID = "1234";
    private static final String ADDRESS_ID_WRONG = "12345";

    @Spy
    @InjectMocks
    private DefaultNuveiAddressFacade testObj;

    @Mock
    private CartService cartServiceMock;
    @Mock
    private DeliveryService deliveryServiceMock;
    @Mock
    private ModelService modelServiceMock;
    @Mock
    private Converter<AddressData, AddressModel> addressReverseConverterMock;

    @Spy
    private AddressData addressDataSpy;
    @Spy
    private CartModel cartModelSpy;
    @Spy
    private AddressModel addressModelSpy, addressModelTwoSpy;

    @Test
    public void createAndSetCartBillingAddress_ShouldCreateAddressAndSetIntoCart_WhenSessionCartIsNotNull() {
        when(cartServiceMock.hasSessionCart()).thenReturn(Boolean.TRUE);
        when(cartServiceMock.getSessionCart()).thenReturn(cartModelSpy);
        when(modelServiceMock.create(AddressModel.class)).thenReturn(addressModelSpy);
        when(addressReverseConverterMock.convert(addressDataSpy)).thenReturn(addressModelSpy);

        testObj.createAndSetCartBillingAddress(addressDataSpy);

        verify(modelServiceMock).save(cartModelSpy);

        assertThat(cartModelSpy.getPaymentAddress()).isEqualTo(addressModelSpy);
        assertThat(addressModelSpy.getBillingAddress()).isTrue();
    }

    @Test(expected = IllegalArgumentException.class)
    public void createAndSetCartBillingAddress_ShouldThrowException_WhenHasNotSessionCartNull() {
        when(cartServiceMock.hasSessionCart()).thenReturn(Boolean.FALSE);

        testObj.createAndSetCartBillingAddress(addressDataSpy);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createAndSetCartBillingAddress_ShouldThrowException_WhenAddressDataIsNull() {
        testObj.createAndSetCartBillingAddress(null);
    }

    @Test
    public void setCartBillingAddress_ShouldSetAddressIntoCart_WhenSessionCartIsNotNull() {
        when(cartServiceMock.hasSessionCart()).thenReturn(Boolean.TRUE);
        when(cartServiceMock.getSessionCart()).thenReturn(cartModelSpy);
        when(addressDataSpy.getId()).thenReturn(ADDRESS_ID);
        doReturn(ADDRESS_PK).when(addressModelSpy).getPk();
        doReturn(ADDRESS_PK).when(addressModelTwoSpy).getPk();
        doReturn(addressModelSpy).when(testObj).getDeliveryAddressModelForCode(ADDRESS_ID, cartModelSpy);

        testObj.setCartBillingAddress(addressDataSpy);

        verify(modelServiceMock).save(cartModelSpy);

        assertThat(cartModelSpy.getPaymentAddress()).isEqualTo(addressModelSpy);
    }

    @Test(expected = IllegalArgumentException.class)
    public void setCartBillingAddress_ShouldThrowException_WhenSessionCartIsNull() {
        when(cartServiceMock.hasSessionCart()).thenReturn(Boolean.FALSE);

        testObj.setCartBillingAddress(addressDataSpy);
    }

    @Test
    public void getDeliveryAddressModelForCode_ShouldReturnAddressModel_WhenAddressIdAndCartModelAreNotNull() {
        when(deliveryServiceMock.getSupportedDeliveryAddressesForOrder(cartModelSpy, Boolean.FALSE)).thenReturn(List.of(addressModelSpy, addressModelTwoSpy));
        doReturn(ADDRESS_PK).when(addressModelSpy).getPk();
        doReturn(ADDRESS_PK).when(addressModelTwoSpy).getPk();

        final AddressModel result = testObj.getDeliveryAddressModelForCode(ADDRESS_ID, cartModelSpy);

        assertThat(result).isNotNull();
    }

    @Test
    public void getDeliveryAddressModelForCode_ShouldReturnNull_WhenAddressIdANotMatch() {
        when(deliveryServiceMock.getSupportedDeliveryAddressesForOrder(cartModelSpy, Boolean.FALSE)).thenReturn(List.of(addressModelSpy, addressModelTwoSpy));
        doReturn(ADDRESS_PK).when(addressModelSpy).getPk();
        doReturn(ADDRESS_PK).when(addressModelTwoSpy).getPk();

        final AddressModel result = testObj.getDeliveryAddressModelForCode(ADDRESS_ID_WRONG, cartModelSpy);

        assertThat(result).isNull();
    }

    @Test
    public void getDeliveryAddressModelForCode_ShouldReturnNull_WhenCartModelIsNull() {
        final AddressModel result = testObj.getDeliveryAddressModelForCode(ADDRESS_ID, null);

        assertThat(result).isNull();
    }

    @Test(expected = IllegalArgumentException.class)
    public void getDeliveryAddressModelForCode_ShouldThrowException_WhenAddressIdIsNull() {
        testObj.getDeliveryAddressModelForCode(null, cartModelSpy);
    }

    @Test
    public void createBillingAddressModel_ShouldReturnAddressModel() {
        when(modelServiceMock.create(AddressModel.class)).thenReturn(addressModelSpy);
        when(addressReverseConverterMock.convert(addressDataSpy)).thenReturn(addressModelSpy);

        final AddressModel result = testObj.createBillingAddressModel(addressDataSpy, cartModelSpy);

        assertThat(addressModelSpy).isEqualTo(result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createBillingAddressModel_ShouldThrowException_WhenAddressDataIsNull() {
        testObj.createBillingAddressModel(null, cartModelSpy);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createBillingAddressModel_ShouldThrowException_WhenCartModelIsNull() {
        testObj.createBillingAddressModel(addressDataSpy, null);
    }
}
