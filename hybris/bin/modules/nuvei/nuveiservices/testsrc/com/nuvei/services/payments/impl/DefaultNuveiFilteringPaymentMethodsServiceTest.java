package com.nuvei.services.payments.impl;

import com.nuvei.services.enums.NuveiHashAlgorithm;
import com.nuvei.services.model.NuveiMerchantConfigurationModel;
import com.nuvei.services.model.NuveiPaymentMethodModel;
import com.safecharge.exception.SafechargeException;
import com.safecharge.model.PaymentMethod;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.model.ModelService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class DefaultNuveiFilteringPaymentMethodsServiceTest {

    private static final String MERCHANT_ID = "merchantId";
    private static final String SERVER_HOST = "serverHost";
    private static final String MERCHANT_SITE_ID = "merchantSiteId";
    private static final String MERCHANT_SECRET_ID = "merchantSecretId";
    private static final String FIRST_PAYMENT_METHOD_ID = "firstMethodId";
    private static final String THIRD_PAYMENT_METHOD_ID = "thirdMethodId";
    private static final String SECOND_PAYMENT_METHOD_ID = "secondMethodId";
    private static final String FIRST_NUVEI_PAYMENT_METHOD_ID = "firstMethodId";
    private static final String SECOND_NUVEI_PAYMENT_METHOD_ID = "secondMethodId";

    @Spy
    @InjectMocks
    private DefaultNuveiFilteringPaymentMethodsService testObj;

    @Mock
    private ModelService modelServiceMock;
    @Mock
    private Converter<PaymentMethod, NuveiPaymentMethodModel> nuveiPaymentMethodsConverterMock;

    final NuveiMerchantConfigurationModel merchantConfiguration = new NuveiMerchantConfigurationModel();

    @Before
    public void setUp() {
        merchantConfiguration.setMerchantId(MERCHANT_ID);
        merchantConfiguration.setMerchantSecretKey(MERCHANT_SECRET_ID);
        merchantConfiguration.setMerchantSiteId(MERCHANT_SITE_ID);
        merchantConfiguration.setServerHost(SERVER_HOST);
        merchantConfiguration.setHashAlgorithm(NuveiHashAlgorithm.SHA256);
    }

    @Test
    public void getPaymentMethodsForMerchant_ShouldImportPaymentsAndDontModifyMerchantPayments_WhenTheImportedPaymentsAreTheSameAsTheMerchant() throws SafechargeException {
        final NuveiPaymentMethodModel firstMethodModel = new NuveiPaymentMethodModel();
        final NuveiPaymentMethodModel secondMethodModel = new NuveiPaymentMethodModel();
        final PaymentMethod firstPaymentMethod = new PaymentMethod();
        final PaymentMethod secondPaymentMethod = new PaymentMethod();
        firstMethodModel.setId(FIRST_NUVEI_PAYMENT_METHOD_ID);
        secondMethodModel.setId(SECOND_NUVEI_PAYMENT_METHOD_ID);
        firstPaymentMethod.setPaymentMethod(FIRST_PAYMENT_METHOD_ID);
        secondPaymentMethod.setPaymentMethod(SECOND_PAYMENT_METHOD_ID);
        merchantConfiguration.setPaymentMethods(Set.of(firstMethodModel, secondMethodModel));

        doReturn(List.of(firstPaymentMethod, secondPaymentMethod))
                .when(testObj).getFilteringMerchantPaymentMethodsResponse(merchantConfiguration);

        testObj.synchFilteringPaymentMethodsForMerchant(merchantConfiguration);

        verify(modelServiceMock).removeAll(Collections.emptySet());
        verify(modelServiceMock, never()).save(merchantConfiguration);
    }

    @Test
    public void getPaymentMethodsForMerchant_ShouldImportPaymentsAndCreateNewNuveiPaymentMethods_WhenNewPaymentMethodsAreImported() throws SafechargeException {
        final NuveiPaymentMethodModel firstMethodModel = new NuveiPaymentMethodModel();
        final NuveiPaymentMethodModel secondMethodModel = new NuveiPaymentMethodModel();
        final NuveiPaymentMethodModel thirdMethodModel = new NuveiPaymentMethodModel();
        final PaymentMethod firstPaymentMethod = new PaymentMethod();
        final PaymentMethod secondPaymentMethod = new PaymentMethod();
        final PaymentMethod thirdPaymentMethod = new PaymentMethod();
        firstMethodModel.setId(FIRST_NUVEI_PAYMENT_METHOD_ID);
        secondMethodModel.setId(SECOND_NUVEI_PAYMENT_METHOD_ID);
        firstPaymentMethod.setPaymentMethod(FIRST_PAYMENT_METHOD_ID);
        secondPaymentMethod.setPaymentMethod(SECOND_PAYMENT_METHOD_ID);
        thirdPaymentMethod.setPaymentMethod(THIRD_PAYMENT_METHOD_ID);
        merchantConfiguration.setPaymentMethods(Set.of(firstMethodModel, secondMethodModel));

        doReturn(List.of(firstPaymentMethod, secondPaymentMethod, thirdPaymentMethod))
                .when(testObj).getFilteringMerchantPaymentMethodsResponse(merchantConfiguration);

        when(nuveiPaymentMethodsConverterMock.convertAll(Set.of(thirdPaymentMethod)))
                .thenReturn(List.of(thirdMethodModel));

        testObj.synchFilteringPaymentMethodsForMerchant(merchantConfiguration);

        verify(modelServiceMock).removeAll(Collections.emptySet());
        verify(modelServiceMock).save(merchantConfiguration);

        assertThat(merchantConfiguration.getPaymentMethods())
                .containsExactlyInAnyOrder(firstMethodModel, secondMethodModel, thirdMethodModel);
    }

    @Test
    public void getPaymentMethodsForMerchant_ShouldImportPaymentMethodsAndRemoveNonexistentOnesFromTheMerchant_WheNonExistentPaymentMethodsAreImported() throws SafechargeException {
        final NuveiPaymentMethodModel firstMethodModel = new NuveiPaymentMethodModel();
        final NuveiPaymentMethodModel secondMethodModel = new NuveiPaymentMethodModel();
        final PaymentMethod firstPaymentMethod = new PaymentMethod();
        firstMethodModel.setId(FIRST_NUVEI_PAYMENT_METHOD_ID);
        secondMethodModel.setId(SECOND_NUVEI_PAYMENT_METHOD_ID);
        firstPaymentMethod.setPaymentMethod(FIRST_PAYMENT_METHOD_ID);
        merchantConfiguration.setPaymentMethods(Set.of(firstMethodModel, secondMethodModel));

        doReturn(List.of(firstPaymentMethod))
                .when(testObj).getFilteringMerchantPaymentMethodsResponse(merchantConfiguration);

        testObj.synchFilteringPaymentMethodsForMerchant(merchantConfiguration);

        verify(modelServiceMock).removeAll(Set.of(secondMethodModel));
        verify(modelServiceMock).refresh(merchantConfiguration);
    }
}
