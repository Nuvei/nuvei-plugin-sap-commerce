package com.nuvei.services.payments.impl;

import com.nuvei.services.model.NuveiPaymentInfoModel;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.user.AddressService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class DefaultNuveiPaymentInfoServiceTest {

    private static final String CART_CODE = "cartCode";

    @InjectMocks
    private DefaultNuveiPaymentInfoService testObj;

    @Mock
    private AddressService addressServiceMock;
    @Mock
    private ModelService modelServiceMock;

    private final CartModel cartModel = new CartModel();
    private final NuveiPaymentInfoModel nuveiPaymentInfoModel = new NuveiPaymentInfoModel();
    private final UserModel userModel = new UserModel();
    private final AddressModel addressModel = new AddressModel();
    private final AddressModel clonedAddressModel = new AddressModel();

    @Captor
    private ArgumentCaptor<String> paymentCodeArgumentCaptor;

    @Test
    public void removePaymentInfo_ShouldRemovePaymentInfo() {
        cartModel.setPaymentInfo(nuveiPaymentInfoModel);

        testObj.removePaymentInfo(cartModel);

        verify(modelServiceMock).remove(nuveiPaymentInfoModel);
        assertThat(cartModel.getPaymentInfo()).isNull();
        verify(modelServiceMock).save(cartModel);
    }

    @Test(expected = IllegalArgumentException.class)
    public void removePaymentInfo_WhenGivenCartIsNull_ShouldThrowException() {
        testObj.removePaymentInfo(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void removePaymentInfo_WhenPaymentInfoOfGivenCartIsNull_ShouldThrowException() {
        testObj.removePaymentInfo(null);
    }

    @Test
    public void createPaymentInfo_ShouldCreatePaymentInfo() {
        cartModel.setUser(userModel);
        cartModel.setCode(CART_CODE);
        cartModel.setPaymentAddress(addressModel);
        when(addressServiceMock.cloneAddressForOwner(addressModel, nuveiPaymentInfoModel))
                .thenReturn(clonedAddressModel);

        testObj.createPaymentInfo(nuveiPaymentInfoModel, cartModel);

        assertThat(clonedAddressModel.getBillingAddress()).isTrue();
        assertThat(clonedAddressModel.getShippingAddress()).isFalse();
        assertThat(clonedAddressModel.getOwner()).isEqualTo(nuveiPaymentInfoModel);
        assertThat(nuveiPaymentInfoModel.getBillingAddress()).isEqualTo(clonedAddressModel);
        assertThat(nuveiPaymentInfoModel.getUser()).isEqualTo(userModel);
        verify(modelServiceMock).save(nuveiPaymentInfoModel);
        assertThat(cartModel.getPaymentInfo()).isEqualTo(nuveiPaymentInfoModel);
        verify(modelServiceMock).save(cartModel);

        assertThat(nuveiPaymentInfoModel.getCode()).startsWith(CART_CODE);

    }

    @Test(expected = IllegalArgumentException.class)
    public void createPaymentInfo_WhenGivenCartIsNull_ShouldThrowException() {
        testObj.createPaymentInfo(nuveiPaymentInfoModel, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createPaymentInfo_WhenPaymentInfoIsNull_ShouldThrowException() {
        testObj.createPaymentInfo(null, cartModel);
    }

    @Test(expected = IllegalArgumentException.class)
    public void cloneAndSetBillingAddressFromCart_WhenGivenCartIsNull_ShouldThrowException() {
        testObj.cloneAndSetBillingAddressFromCart(null, nuveiPaymentInfoModel);
    }

    @Test(expected = IllegalArgumentException.class)
    public void cloneAndSetBillingAddressFromCart_WhenPaymentInfoIsNull_ShouldThrowException() {
        testObj.cloneAndSetBillingAddressFromCart(cartModel, null);
    }
}
