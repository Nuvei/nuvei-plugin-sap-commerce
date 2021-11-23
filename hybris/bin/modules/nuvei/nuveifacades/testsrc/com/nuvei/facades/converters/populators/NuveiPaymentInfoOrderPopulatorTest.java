package com.nuvei.facades.converters.populators;

import com.nuvei.services.model.NuveiPaymentInfoModel;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.commercefacades.order.data.AbstractOrderData;
import de.hybris.platform.commercefacades.order.data.CCPaymentInfoData;
import de.hybris.platform.commercefacades.order.data.OrderData;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.order.payment.PaymentInfoModel;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class NuveiPaymentInfoOrderPopulatorTest {

    @InjectMocks
    private NuveiPaymentInfoOrderPopulator testObj;

    @Mock
    private Converter<NuveiPaymentInfoModel, CCPaymentInfoData> nuveiPaymentInfoDataConverter;

    private AbstractOrderModel source = new OrderModel();
    private AbstractOrderData target = new OrderData();
    private CCPaymentInfoData paymentInfoData = new CCPaymentInfoData();

    @Test
    public void populate_ShouldPopulatePaymentInfoData_WhenSourceHasPaymentInfoOfTypeNuveiPaymentInfo() {
        final NuveiPaymentInfoModel paymentInfoModel = new NuveiPaymentInfoModel();
        source.setPaymentInfo(paymentInfoModel);

        when(nuveiPaymentInfoDataConverter.convert(paymentInfoModel)).thenReturn(paymentInfoData);

        testObj.populate(source, target);

        assertThat(target.getPaymentInfo()).isEqualTo(paymentInfoData);
    }

    @Test
    public void populate_ShouldNotPopulatePaymentInfoData_WhenSourceHasNoPaymentInfo() {
        testObj.populate(source, target);

        assertThat(target.getPaymentInfo()).isNull();
    }

    @Test
    public void populate_ShouldNotPopulatePaymentInfoData_WhenSourceHasNoPaymentInfoOfTypeNuveiPaymentInfo() {
        final PaymentInfoModel paymentInfoModel = new PaymentInfoModel();
        source.setPaymentInfo(paymentInfoModel);

        testObj.populate(source, target);

        assertThat(target.getPaymentInfo()).isNull();
    }
}
