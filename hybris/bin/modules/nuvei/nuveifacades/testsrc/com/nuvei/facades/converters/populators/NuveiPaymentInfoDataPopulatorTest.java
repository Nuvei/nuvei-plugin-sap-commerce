package com.nuvei.facades.converters.populators;

import com.nuvei.services.model.NuveiPaymentInfoModel;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.commercefacades.order.data.CCPaymentInfoData;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.core.PK;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class NuveiPaymentInfoDataPopulatorTest {

    private static final String CARD_NUMBER = "cardNumber";
    private static final String EXP_MONTH = "expMonth";
    private static final String EXP_YEAR = "expYear";

    @InjectMocks
    private NuveiPaymentInfoDataPopulator testObj;

    @Mock
    protected Converter<AddressModel, AddressData> addressConverterMock;

    @Mock
    private NuveiPaymentInfoModel sourceMock;

    private final CCPaymentInfoData target = new CCPaymentInfoData();
    private final AddressData addressData = new AddressData();
    private final AddressModel addressModel = new AddressModel();

    @Before
    public void setUp() {
        final PK pk = PK.fromLong(123L);

        when(sourceMock.getMaskedCardNumber()).thenReturn(CARD_NUMBER);
        when(sourceMock.getExpMonth()).thenReturn(EXP_MONTH);
        when(sourceMock.getExpYear()).thenReturn(EXP_YEAR);
        when(sourceMock.getBillingAddress()).thenReturn(addressModel);
        when(sourceMock.getPk()).thenReturn(pk);
        when(sourceMock.getBillingAddress()).thenReturn(addressModel);
        when(addressConverterMock.convert(addressModel)).thenReturn(addressData);
    }

    @Test
    public void populate_ShouldPopulateCardFieldsAndAddress_WhenSourceHasAddress() {
        testObj.populate(sourceMock, target);

        assertThat(target.getCardNumber()).isEqualTo(CARD_NUMBER);
        assertThat(target.getExpiryMonth()).isEqualTo(EXP_MONTH);
        assertThat(target.getExpiryYear()).isEqualTo(EXP_YEAR);
        assertThat(target.getBillingAddress()).isEqualTo(addressData);
    }

    @Test
    public void populate_ShouldPopulateOnlyCardFields_WhenSourceHasNoAddress() {
        when(addressConverterMock.convert(addressModel)).thenReturn(null);

        testObj.populate(sourceMock, target);

        assertThat(target.getCardNumber()).isEqualTo(CARD_NUMBER);
        assertThat(target.getExpiryMonth()).isEqualTo(EXP_MONTH);
        assertThat(target.getExpiryYear()).isEqualTo(EXP_YEAR);
        assertThat(target.getBillingAddress()).isNull();
    }
}
