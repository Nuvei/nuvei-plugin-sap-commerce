package com.nuvei.services.service.impl;

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.payment.enums.PaymentTransactionType;
import de.hybris.platform.payment.model.PaymentTransactionEntryModel;
import de.hybris.platform.servicelayer.internal.dao.DefaultGenericDao;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class DefaultNuveiPaymentTransactionEntryServiceTest {

    private static final String REQUEST_ID = "requestId";
    private static final String VERSION_ID = "versionId";

    @InjectMocks
    private DefaultNuveiPaymentTransactionEntryService testObj;

    @Mock
    private DefaultGenericDao<PaymentTransactionEntryModel> paymentTransactionEntryGenericDaoMock;

    @Mock
    private PaymentTransactionEntryModel paymentTransactionEntryModelMock, paymentTransactionEntryModelTwoMock;

    @Test
    public void findAuthorizationPaymentEntryByRequestID_shouldReturnPaymentTransactionEntryModel_whenRequestIdMatchesOnce() {
        when(paymentTransactionEntryGenericDaoMock.find(
                Map.of(
                        PaymentTransactionEntryModel.REQUESTID, REQUEST_ID,
                        PaymentTransactionEntryModel.TYPE, PaymentTransactionType.AUTHORIZATION
                ))
        ).thenReturn(Collections.singletonList(paymentTransactionEntryModelMock));

        final PaymentTransactionEntryModel result = testObj.findPaymentEntryByRequestIdAndType(REQUEST_ID, PaymentTransactionType.AUTHORIZATION);

        assertThat(result).isEqualTo(paymentTransactionEntryModelMock);
    }

    @Test(expected = IllegalArgumentException.class)
    public void findAuthorizationPaymentEntryByRequestID_shouldThrowException_whenRequestIdIsNull() {
        testObj.findPaymentEntryByRequestIdAndType(null, PaymentTransactionType.AUTHORIZATION);
    }

    @Test
    public void findAuthorizationPaymentEntryByRequestID_shouldReturnNull_whenRequestIdMatchesMoreThanOnce() {
        when(paymentTransactionEntryGenericDaoMock.find(
                Map.of(
                        PaymentTransactionEntryModel.REQUESTID, REQUEST_ID,
                        PaymentTransactionEntryModel.TYPE, PaymentTransactionType.AUTHORIZATION
                ))
        ).thenReturn(List.of(paymentTransactionEntryModelMock, paymentTransactionEntryModelTwoMock));

        final PaymentTransactionEntryModel result = testObj.findPaymentEntryByRequestIdAndType(REQUEST_ID, PaymentTransactionType.AUTHORIZATION);

        assertThat(result).isNull();
    }

    @Test
    public void findAuthorizationPaymentEntryByRequestID_shouldReturnNull_whenRequestIdMatchesOnceAndHasVersionId() {
        when(paymentTransactionEntryGenericDaoMock.find(
                Map.of(
                        PaymentTransactionEntryModel.REQUESTID, REQUEST_ID,
                        PaymentTransactionEntryModel.TYPE, PaymentTransactionType.AUTHORIZATION
                ))
        ).thenReturn(Collections.singletonList(paymentTransactionEntryModelMock));
        when(paymentTransactionEntryModelMock.getVersionID()).thenReturn(VERSION_ID);


        final PaymentTransactionEntryModel result = testObj.findPaymentEntryByRequestIdAndType(REQUEST_ID, PaymentTransactionType.AUTHORIZATION);

        assertThat(result).isNull();
    }


    @Test
    public void findAuthorizationPaymentEntryByRequestID_shouldReturnNull_whenRequestIdNoMatches() {
        when(paymentTransactionEntryGenericDaoMock.find(
                Map.of(
                        PaymentTransactionEntryModel.REQUESTID, REQUEST_ID,
                        PaymentTransactionEntryModel.TYPE, PaymentTransactionType.AUTHORIZATION
                ))
        ).thenReturn(Collections.emptyList());

        final PaymentTransactionEntryModel result = testObj.findPaymentEntryByRequestIdAndType(REQUEST_ID, PaymentTransactionType.AUTHORIZATION);

        assertThat(result).isNull();
    }
}
