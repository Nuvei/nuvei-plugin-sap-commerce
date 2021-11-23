package com.nuvei.facades.converters.populators;

import com.nuvei.facades.beans.NuveiSDKResponseData;
import com.nuvei.services.model.NuveiPaymentInfoModel;
import de.hybris.bootstrap.annotations.UnitTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class NuveiPaymentInfoReversePopulatorTest {

    private static final String EXP_YEAR = "expYear";
    private static final String EXP_MONTH = "expMonth";
    private static final String CARD_NUMBER = "cardNumber";
    private static final String TRANSACTION_ID = "transactionId";

    @InjectMocks
    private NuveiPaymentInfoReversePopulator testObj;

    @Mock
    private NuveiSDKResponseData sourceMock;
    @Mock
    private NuveiPaymentInfoModel targetMock;

    @Test
    public void populate_ShouldPopulateSourceToTarget() {
        when(sourceMock.getCcExpYear()).thenReturn(EXP_YEAR);
        when(sourceMock.getCcExpMonth()).thenReturn(EXP_MONTH);
        when(sourceMock.getCcCardNumber()).thenReturn(CARD_NUMBER);
        when(sourceMock.getTransactionId()).thenReturn(TRANSACTION_ID);

        testObj.populate(sourceMock, targetMock);

        verify(targetMock).setExpYear(EXP_YEAR);
        verify(targetMock).setExpMonth(EXP_MONTH);
        verify(targetMock).setMaskedCardNumber(CARD_NUMBER);
        verify(targetMock).setTransactionId(TRANSACTION_ID);
    }

    @Test(expected = IllegalArgumentException.class)
    public void populate_WhenSourceIsNull_ShouldThrowException() {
        testObj.populate(null, targetMock);
    }

    @Test(expected = IllegalArgumentException.class)
    public void populate_WhenTargetIsNull_ShouldThrowException() {
        testObj.populate(sourceMock, null);
    }
}
