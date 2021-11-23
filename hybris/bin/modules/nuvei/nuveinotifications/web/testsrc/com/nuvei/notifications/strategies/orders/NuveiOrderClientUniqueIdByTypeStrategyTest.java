package com.nuvei.notifications.strategies.orders;

import com.nuvei.notifications.model.NuveiDirectMerchantNotificationModel;
import com.nuvei.services.enums.NuveiTransactionType;
import com.nuvei.services.service.NuveiOrderService;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class NuveiOrderClientUniqueIdByTypeStrategyTest {

    private static final String MERCHANT_UNIQUE_ID = "merchantUniqueId";

    @InjectMocks
    private NuveiOrderClientUniqueIdByTypeStrategy testObj;

    @Mock
    private NuveiOrderService nuveiOrderServiceMock;

    @Mock
    private NuveiDirectMerchantNotificationModel nuveiDirectMerchantNotificationModelMock;
    @Mock
    private AbstractOrderModel abstractOrderModelMock;

    @Test
    public void isApplicable_shouldReturnTrue_whenSourceIsEqualsToAUTH() {
        final boolean result = testObj.isApplicable(Pair.of(NuveiTransactionType.AUTH, nuveiDirectMerchantNotificationModelMock));

        assertThat(result).isTrue();
    }

    @Test
    public void isApplicable_shouldReturnTrue_whenSourceIsEqualsToSALE() {
        final boolean result = testObj.isApplicable(Pair.of(NuveiTransactionType.SALE, nuveiDirectMerchantNotificationModelMock));

        assertThat(result).isTrue();
    }

    @Test
    public void isApplicable_shouldReturnTrue_whenSourceIsEqualsSETTLE() {
        final boolean result = testObj.isApplicable(Pair.of(NuveiTransactionType.SETTLE, nuveiDirectMerchantNotificationModelMock));

        assertThat(result).isFalse();
    }

    @Test
    public void execute_shouldReturnAbstractOrderModel_WhenAbstractOrderModelIsFound() {
        when(nuveiDirectMerchantNotificationModelMock.getMerchantUniqueId()).thenReturn(MERCHANT_UNIQUE_ID);
        when(nuveiOrderServiceMock.findAbstractOrderModelByClientUniqueId(MERCHANT_UNIQUE_ID)).thenReturn(abstractOrderModelMock);

        final AbstractOrderModel result = testObj.execute(Pair.of(NuveiTransactionType.AUTH, nuveiDirectMerchantNotificationModelMock));

        assertThat(result).isEqualTo(abstractOrderModelMock);
    }

    @Test
    public void execute_shouldReturnNull_WhenAbstractOrderModelIsNotFound() {
        when(nuveiOrderServiceMock.findAbstractOrderModelByClientUniqueId(MERCHANT_UNIQUE_ID)).thenReturn(null);

        final AbstractOrderModel result = testObj.execute(Pair.of(NuveiTransactionType.AUTH, nuveiDirectMerchantNotificationModelMock));

        assertThat(result).isNull();
    }

}
