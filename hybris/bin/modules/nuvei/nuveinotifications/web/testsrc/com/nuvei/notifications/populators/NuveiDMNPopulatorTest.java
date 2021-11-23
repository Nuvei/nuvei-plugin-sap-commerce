package com.nuvei.notifications.populators;

import com.nuvei.notifications.data.NuveiIncomingDMNData;
import com.nuvei.notifications.enums.NuveiPPPTransactionStatus;
import com.nuvei.notifications.enums.NuveiTransactionStatus;
import com.nuvei.notifications.model.NuveiDirectMerchantNotificationModel;
import com.nuvei.services.enums.NuveiTransactionType;
import com.nuvei.services.model.NuveiPaymentMethodModel;
import com.nuvei.services.service.NuveiPaymentMethodService;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.enumeration.EnumerationService;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class NuveiDMNPopulatorTest {

    private static final String TRANSACTION_ID = "1111";
    private static final String PPP_TRANSACTION_ID = "2222";
    private static final String APPROVED = "APPROVED";
    private static final String OK = "OK";
    private static final String CC_CARD = "cc_card";
    private static final String MERCHANT_ID = "merchantId";
    private static final String MERCHANT_SITE_ID = "merchantSiteId";
    private static final String TRANSACTION_TYPE = "transactionType";
    private static final String CURRENCY = "currency";
    private static final String SYSTEM_DECISION = "SystemDecision";
    private static final String TOTAL_AMOUNT = "12.00";
    private static final String FINAL_DECISION = "finalDecision";

    @InjectMocks
    private NuveiDMNPopulator testObj;

    @Mock
    private EnumerationService enumerationServiceMock;
    @Mock
    private NuveiPaymentMethodService nuveiPaymentMethodServiceMock;
    @Mock
    private CommonI18NService commonI18NServiceMock;

    @Mock
    private NuveiPaymentMethodModel nuveiPaymentMethodMock;
    @Mock
    private CurrencyModel currencyModelMock;

    private final NuveiIncomingDMNData nuveiIncomingDataStub = new NuveiIncomingDMNData();
    private final NuveiDirectMerchantNotificationModel nuveiDMNModelStub = new NuveiDirectMerchantNotificationModel();

    @Test
    public void populate_shouldPopulateNuveiDMNModel() {
        nuveiIncomingDataStub.setTransactionID(TRANSACTION_ID);
        nuveiIncomingDataStub.setPPP_TransactionID(PPP_TRANSACTION_ID);
        nuveiIncomingDataStub.setStatus(APPROVED);
        nuveiIncomingDataStub.setPpp_status(OK);
        nuveiIncomingDataStub.setPayment_method(CC_CARD);
        nuveiIncomingDataStub.setMerchant_id(MERCHANT_ID);
        nuveiIncomingDataStub.setMerchant_site_id(MERCHANT_SITE_ID);
        nuveiIncomingDataStub.setTransactionType(TRANSACTION_TYPE);
        nuveiIncomingDataStub.setCurrency(CURRENCY);
        nuveiIncomingDataStub.setTotalAmount(TOTAL_AMOUNT);
        nuveiIncomingDataStub.setFinalFraudDecision(FINAL_DECISION);
        nuveiIncomingDataStub.setSystemDecision(SYSTEM_DECISION);

        when(nuveiPaymentMethodServiceMock.findNuveiPaymentMethodById(CC_CARD)).thenReturn(nuveiPaymentMethodMock);
        when(enumerationServiceMock.getEnumerationValue(NuveiTransactionType.class, TRANSACTION_TYPE)).thenReturn(NuveiTransactionType.CREDIT);
        when(enumerationServiceMock.getEnumerationValue(NuveiTransactionStatus.class, APPROVED)).thenReturn(NuveiTransactionStatus.APPROVED);
        when(enumerationServiceMock.getEnumerationValue(NuveiPPPTransactionStatus.class, OK)).thenReturn(NuveiPPPTransactionStatus.OK);
        when(commonI18NServiceMock.getCurrency(CURRENCY)).thenReturn(currencyModelMock);
        when(currencyModelMock.getIsocode()).thenReturn(CURRENCY);

        testObj.populate(nuveiIncomingDataStub, nuveiDMNModelStub);

        assertThat(nuveiDMNModelStub.getTransactionId()).isEqualTo(TRANSACTION_ID);
        assertThat(nuveiDMNModelStub.getPppTransactionId()).isEqualTo(PPP_TRANSACTION_ID);
        assertThat(nuveiDMNModelStub.getStatus()).isEqualTo(NuveiTransactionStatus.APPROVED);
        assertThat(nuveiDMNModelStub.getPppStatus()).isEqualTo(NuveiPPPTransactionStatus.OK);
        assertThat(nuveiDMNModelStub.getPaymentMethod()).isEqualTo(nuveiPaymentMethodMock);
        assertThat(nuveiDMNModelStub.getMerchantId()).isEqualTo(MERCHANT_ID);
        assertThat(nuveiDMNModelStub.getMerchantSiteId()).isEqualTo(MERCHANT_SITE_ID);
        assertThat(nuveiDMNModelStub.getTransactionType()).isEqualTo(NuveiTransactionType.CREDIT);
        assertThat(nuveiDMNModelStub.getRawNotification()).isEqualTo(nuveiIncomingDataStub);
        assertThat(nuveiDMNModelStub.getFinalFraudDecision()).isEqualTo(FINAL_DECISION);
        assertThat(nuveiDMNModelStub.getSystemDecision()).isEqualTo(SYSTEM_DECISION);
        assertThat(nuveiDMNModelStub.getTotalAmount().toString()).hasToString(TOTAL_AMOUNT);
        assertThat(nuveiDMNModelStub.getCurrency().getIsocode()).isEqualTo(CURRENCY);

        verify(nuveiPaymentMethodServiceMock).findNuveiPaymentMethodById(CC_CARD);
        verify(enumerationServiceMock).getEnumerationValue(NuveiTransactionType.class, TRANSACTION_TYPE);
        verify(enumerationServiceMock).getEnumerationValue(NuveiTransactionStatus.class, APPROVED);
        verify(enumerationServiceMock).getEnumerationValue(NuveiPPPTransactionStatus.class, OK);
    }

    @Test
    public void populate_shouldNotPopulateFields_whenFieldsAreNull() {
        nuveiIncomingDataStub.setMerchant_id(MERCHANT_ID);
        nuveiIncomingDataStub.setMerchant_site_id(MERCHANT_SITE_ID);

        testObj.populate(nuveiIncomingDataStub, nuveiDMNModelStub);

        assertThat(nuveiDMNModelStub.getMerchantId()).isEqualTo(MERCHANT_ID);
        assertThat(nuveiDMNModelStub.getMerchantSiteId()).isEqualTo(MERCHANT_SITE_ID);
        assertThat(nuveiDMNModelStub.getRawNotification()).isEqualTo(nuveiIncomingDataStub);
        assertThat(nuveiDMNModelStub.getTransactionId()).isNull();
        assertThat(nuveiDMNModelStub.getPppTransactionId()).isNull();
        assertThat(nuveiDMNModelStub.getStatus()).isNull();
        assertThat(nuveiDMNModelStub.getPppStatus()).isNull();
        assertThat(nuveiDMNModelStub.getPaymentMethod()).isNull();
        assertThat(nuveiDMNModelStub.getTransactionType()).isNull();
        assertThat(nuveiDMNModelStub.getFinalFraudDecision()).isNull();
        assertThat(nuveiDMNModelStub.getSystemDecision()).isNull();
        assertThat(nuveiDMNModelStub.getCurrency()).isNull();
        assertThat(nuveiDMNModelStub.getTotalAmount()).isNull();

        verifyZeroInteractions(nuveiPaymentMethodServiceMock);
        verifyZeroInteractions(enumerationServiceMock);
    }

    @Test(expected = IllegalArgumentException.class)
    public void populate_shouldThrowException_whenSourceIsNull() {
        testObj.populate(null, nuveiDMNModelStub);
    }

    @Test(expected = IllegalArgumentException.class)
    public void populate_shouldThrowException_whenTargetIsNull() {
        testObj.populate(nuveiIncomingDataStub, null);
    }
}
