package com.nuvei.notifications.checksum;

import com.nuvei.notifications.data.NuveiIncomingDMNData;
import com.nuvei.services.merchant.NuveiMerchantConfigurationService;
import com.nuvei.services.model.NuveiMerchantConfigurationModel;
import de.hybris.bootstrap.annotations.UnitTest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Method;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.assertj.core.api.Java6Assertions.assertThatThrownBy;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class NuveiValidateChecksumAspectTest {
    private static final String VALID_ANNOTATED_METHOD = "validAnnotatedMethod";
    private static final String INVALID_ANNOTATED_METHOD = "invalidAnnotatedMethod";
    private static final String MERCHANT_ID = "MERCHANT_ID";
    private static final String SITE_ID = "SITE_ID";
    private static final String MERCHANT_SECRET_KEY = "THE_SECRET_KEY";

    @Spy
    @InjectMocks
    private NuveiValidateChecksumAspect testObj;

    @Mock
    private ProceedingJoinPoint pjpMock;
    @Mock
    private MethodSignature signatureMock;
    @Mock
    private NuveiMerchantConfigurationService nuveiMerchantConfigurationServiceMock;

    @Before
    public void setUp() throws Exception {
        when(pjpMock.getSignature()).thenReturn(signatureMock);
        final Method annotatedMethod = MyControllerTestClass.class.getMethod(VALID_ANNOTATED_METHOD, NuveiIncomingDMNData.class);
        when(signatureMock.getMethod()).thenReturn(annotatedMethod);
        when(pjpMock.getTarget()).thenReturn(new MyControllerTestClass());
    }

    @Test
    public void validateChecksum_shouldProceedWhenInternalValidateChecksumIsValid() throws Throwable {
        when(pjpMock.getSignature()).thenReturn(signatureMock);
        final Method annotatedMethod = MyControllerTestClass.class.getMethod(VALID_ANNOTATED_METHOD, NuveiIncomingDMNData.class);
        when(signatureMock.getMethod()).thenReturn(annotatedMethod);
        when(pjpMock.getTarget()).thenReturn(new MyControllerTestClass());
        final Object pjpProceedObject = new Object();
        when(pjpMock.proceed()).thenReturn(pjpProceedObject);
        doReturn(true).when(testObj).internalValidateChecksum(any(Integer.class), any(Object[].class));

        final Object result = testObj.validateChecksum(pjpMock);

        assertThat(result).isEqualTo(pjpProceedObject);
        verify(pjpMock).proceed();
    }

    @Test
    public void validateChecksum_shouldThrowResponseStatusExceptionWhenInternalValidateChecksumIsInvalid() throws Throwable {
        when(pjpMock.getSignature()).thenReturn(signatureMock);
        final Method annotatedMethod = MyControllerTestClass.class.getMethod(VALID_ANNOTATED_METHOD, NuveiIncomingDMNData.class);
        when(signatureMock.getMethod()).thenReturn(annotatedMethod);
        when(pjpMock.getTarget()).thenReturn(new MyControllerTestClass());
        final Object pjpProceedObject = new Object();
        when(pjpMock.proceed()).thenReturn(pjpProceedObject);
        doReturn(false).when(testObj).internalValidateChecksum(any(Integer.class), any(Object[].class));

        assertThatThrownBy(() -> testObj.validateChecksum(pjpMock))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessage("422 UNPROCESSABLE_ENTITY \"The validation or the processing of the checksum has failed\"");

        verify(pjpMock, never()).proceed();
    }

    @Test
    public void internalValidateChecksum_shouldReturnTrueWhenParamIsNuveiIncomingDMNDataWithStatusAndChecksumIsValid() throws NoSuchMethodException {
        final NuveiIncomingDMNData validIncomingNotificationData = createValidIncomingNotificationDataWithStatus();
        final NuveiIncomingDMNData[] validIncomingNotificationsData = {validIncomingNotificationData};
        final NuveiMerchantConfigurationModel nuveiMerchantConfigurationModelStub = new NuveiMerchantConfigurationModel();
        nuveiMerchantConfigurationModelStub.setMerchantSecretKey(MERCHANT_SECRET_KEY);
        when(nuveiMerchantConfigurationServiceMock.getMerchantConfigurationByMerchantIdAndSiteId(validIncomingNotificationData.getMerchant_id(), validIncomingNotificationData.getMerchant_site_id()))
                .thenReturn(nuveiMerchantConfigurationModelStub);

        final boolean validChecksum = testObj.internalValidateChecksum(0, validIncomingNotificationsData);

        assertThat(validChecksum).isTrue();
    }

    @Test
    public void internalValidateChecksum_shouldReturnTrueWhenParamIsNuveiIncomingDMNDataWithNoStatusAndChecksumIsValid() throws NoSuchMethodException {
        final NuveiIncomingDMNData validIncomingNotificationData = createValidIncomingNotificationDataWithNoStatus();
        final NuveiIncomingDMNData[] validIncomingNotificationsData = {validIncomingNotificationData};
        final NuveiMerchantConfigurationModel nuveiMerchantConfigurationModelStub = new NuveiMerchantConfigurationModel();
        nuveiMerchantConfigurationModelStub.setMerchantSecretKey(MERCHANT_SECRET_KEY);
        when(nuveiMerchantConfigurationServiceMock.getMerchantConfigurationByMerchantIdAndSiteId(validIncomingNotificationData.getMerchant_id(), validIncomingNotificationData.getMerchant_site_id()))
                .thenReturn(nuveiMerchantConfigurationModelStub);

        final boolean validChecksum = testObj.internalValidateChecksum(0, validIncomingNotificationsData);

        assertThat(validChecksum).isTrue();
    }

    @Test
    public void internalValidateChecksum_shouldReturnFalseWhenParamIsNotNuveiIncomingDMNData() throws Throwable {
        when(pjpMock.getSignature()).thenReturn(signatureMock);
        final Method annotatedMethod = MyControllerTestClass.class.getMethod(INVALID_ANNOTATED_METHOD, String.class);
        when(signatureMock.getMethod()).thenReturn(annotatedMethod);
        when(pjpMock.getTarget()).thenReturn(new MyControllerTestClass());

        assertThatThrownBy(() -> testObj.validateChecksum(pjpMock))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessage("422 UNPROCESSABLE_ENTITY \"The validation or the processing of the checksum has failed\"");

        verify(pjpMock, never()).proceed();
    }

    private NuveiIncomingDMNData createValidIncomingNotificationDataWithStatus() {
        final NuveiIncomingDMNData validIncomingNotificationData = createValidIncomingNotificationDataWithNoStatus();

        validIncomingNotificationData.setMerchant_site_id(SITE_ID);

        return validIncomingNotificationData;
    }

    private NuveiIncomingDMNData createValidIncomingNotificationDataWithNoStatus() {
        final NuveiIncomingDMNData validIncomingNotificationData = new NuveiIncomingDMNData();
        validIncomingNotificationData.setTotalAmount("115");
        validIncomingNotificationData.setCurrency("USD");
        validIncomingNotificationData.setResponseTimeStamp("2020-03-14.16:22:34");
        validIncomingNotificationData.setPPP_TransactionID("3453459");
        validIncomingNotificationData.setStatus("APPROVED");
        validIncomingNotificationData.setProductId("NA");
        validIncomingNotificationData.setAdvanceResponseChecksum("aaa9ad16c8ed9f172dff505e4eeb8929b9c77cfed3e38127d55d42e33a70c762");
        validIncomingNotificationData.setMerchant_id(MERCHANT_ID);
        validIncomingNotificationData.setMerchant_site_id(SITE_ID);

        return validIncomingNotificationData;
    }

    class MyControllerTestClass {
        public void validAnnotatedMethod(@NuveiValidateChecksum final NuveiIncomingDMNData incomingDMNData) {
        }

        public void invalidAnnotatedMethod(@NuveiValidateChecksum final String incomingDMNData) {
        }
    }
}
