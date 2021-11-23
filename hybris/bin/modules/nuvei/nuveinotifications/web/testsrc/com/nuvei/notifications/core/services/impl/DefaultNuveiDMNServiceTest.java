package com.nuvei.notifications.core.services.impl;

import com.nuvei.notifications.dao.NuveiDMNDao;
import com.nuvei.notifications.model.NuveiDirectMerchantNotificationModel;
import com.nuvei.services.enums.NuveiTransactionType;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.servicelayer.exceptions.AmbiguousIdentifierException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class DefaultNuveiDMNServiceTest {

    private static final String PPP_TRANSACTION_ID = "pppTransactionId";
    @InjectMocks
    private DefaultNuveiDMNService testObj;

    @Mock
    private NuveiDMNDao nuveiDMNDaoMock;

    @Mock
    private NuveiDirectMerchantNotificationModel nuveiDirectMerchantNotificationOneModelMock, nuveiDirectMerchantNotificationTwoModelMock;

    @Test
    public void getUnprocessedDMNsByType_shouldReturnListNuveiDirectMerchantNotificationModel_whenFindResults() {
        final Map<String, Object> objectMap = new HashMap<>();
        objectMap.put(NuveiDirectMerchantNotificationModel.PROCESSED, Boolean.FALSE);
        objectMap.put(NuveiDirectMerchantNotificationModel.TRANSACTIONTYPE, NuveiTransactionType.AUTH);

        when(nuveiDMNDaoMock.find(objectMap)).thenReturn(List.of(nuveiDirectMerchantNotificationOneModelMock, nuveiDirectMerchantNotificationTwoModelMock));

        final List<NuveiDirectMerchantNotificationModel> result = testObj.getUnprocessedDMNsByType(NuveiTransactionType.AUTH);

        assertThat(result).containsExactly(nuveiDirectMerchantNotificationOneModelMock, nuveiDirectMerchantNotificationTwoModelMock);
    }

    @Test
    public void getUnprocessedDMNsByType_shouldReturnNull_whenNotFindResults() {
        final Map<String, Object> objectMap = new HashMap<>();
        objectMap.put(NuveiDirectMerchantNotificationModel.PROCESSED, Boolean.FALSE);
        objectMap.put(NuveiDirectMerchantNotificationModel.TRANSACTIONTYPE, NuveiTransactionType.AUTH);

        when(nuveiDMNDaoMock.find(objectMap)).thenReturn(null);

        final List<NuveiDirectMerchantNotificationModel> result = testObj.getUnprocessedDMNsByType(NuveiTransactionType.AUTH);

        assertThat(result).isNull();
    }

    @Test
    public void findDMNByTypePPPTransactionId_ShouldReturnNuveiDirectMerchantNotificationModel_WhenOneModelMatches() {
        final Map<String, Object> objectMap = new HashMap<>();
        objectMap.put(NuveiDirectMerchantNotificationModel.PPPTRANSACTIONID, PPP_TRANSACTION_ID);

        when(nuveiDMNDaoMock.find(objectMap)).thenReturn(Collections.singletonList(nuveiDirectMerchantNotificationOneModelMock));

        final NuveiDirectMerchantNotificationModel result = testObj.findDMNByTypePPPTransactionId(PPP_TRANSACTION_ID);

        assertThat(result).isEqualTo(nuveiDirectMerchantNotificationOneModelMock);
    }

    @Test
    public void findDMNByTypePPPTransactionId_ShouldNull_WhenAnyModelMatches() {
        final Map<String, Object> objectMap = new HashMap<>();
        objectMap.put(NuveiDirectMerchantNotificationModel.PPPTRANSACTIONID, PPP_TRANSACTION_ID);

        when(nuveiDMNDaoMock.find(objectMap)).thenReturn(Collections.emptyList());

        final NuveiDirectMerchantNotificationModel result = testObj.findDMNByTypePPPTransactionId(PPP_TRANSACTION_ID);

        assertThat(result).isNull();
    }

    @Test(expected = AmbiguousIdentifierException.class)
    public void findDMNByTypePPPTransactionId_ShouldThrowException_WhenMoreThanOneModelMatches() {
        final Map<String, Object> objectMap = new HashMap<>();
        objectMap.put(NuveiDirectMerchantNotificationModel.PPPTRANSACTIONID, PPP_TRANSACTION_ID);

        when(nuveiDMNDaoMock.find(objectMap)).thenReturn(List.of(nuveiDirectMerchantNotificationOneModelMock, nuveiDirectMerchantNotificationTwoModelMock));

       testObj.findDMNByTypePPPTransactionId(PPP_TRANSACTION_ID);
    }

    @Test
    public void findDMNToDelete_ShouldReturnAllDMNToDelete_WhenDMNMatchesConditionsToBeDeleted() {
        when(nuveiDMNDaoMock.findDMNToDelete()).thenReturn(List.of(nuveiDirectMerchantNotificationOneModelMock, nuveiDirectMerchantNotificationTwoModelMock));

        final List<NuveiDirectMerchantNotificationModel> dmnToDelete = testObj.findDMNToDelete();

        assertThat(dmnToDelete).isEqualTo(List.of(nuveiDirectMerchantNotificationOneModelMock, nuveiDirectMerchantNotificationTwoModelMock));
    }
}
