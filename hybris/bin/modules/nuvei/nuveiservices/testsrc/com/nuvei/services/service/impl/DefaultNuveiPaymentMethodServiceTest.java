package com.nuvei.services.service.impl;

import com.nuvei.services.model.NuveiPaymentMethodModel;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.servicelayer.internal.dao.GenericDao;
import org.jaxen.util.SingletonList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class DefaultNuveiPaymentMethodServiceTest {

    private static final String ID = "id";
    @InjectMocks
    private DefaultNuveiPaymentMethodService testObj;

    @Mock
    private GenericDao<NuveiPaymentMethodModel> nuveiPaymentMethodGenericDao;

    @Mock
    private NuveiPaymentMethodModel nuveiPaymentMethodOneModel, nuveiPaymentMethodTwoModel;

    @Test
    public void findNuveiPaymentMethodById_shouldReturnNuveiPaymentMethodModel_whenIdMatch() {
        when(nuveiPaymentMethodGenericDao.find(Collections.singletonMap(NuveiPaymentMethodModel.ID, ID))).thenReturn(Collections.singletonList(nuveiPaymentMethodOneModel));

        final NuveiPaymentMethodModel result = testObj.findNuveiPaymentMethodById(ID);

        assertThat(result).isEqualTo(nuveiPaymentMethodOneModel);
    }

    @Test
    public void findNuveiPaymentMethodById_shouldReturnNuveiPaymentMethodModel_whenIdHasMultipleMatches() {
        when(nuveiPaymentMethodGenericDao.find(Collections.singletonMap(NuveiPaymentMethodModel.ID, ID))).thenReturn(List.of(nuveiPaymentMethodOneModel, nuveiPaymentMethodTwoModel));

        final NuveiPaymentMethodModel result = testObj.findNuveiPaymentMethodById(ID);

        assertThat(result).isEqualTo(nuveiPaymentMethodOneModel);
    }

    @Test
    public void findNuveiPaymentMethodById_shouldReturnNull_whenIdIsNull() {
        when(nuveiPaymentMethodGenericDao.find(Collections.singletonMap(NuveiPaymentMethodModel.ID, ID))).thenReturn(Collections.EMPTY_LIST);

        final NuveiPaymentMethodModel result = testObj.findNuveiPaymentMethodById(null);

        assertThat(result).isNull();
    }
}
