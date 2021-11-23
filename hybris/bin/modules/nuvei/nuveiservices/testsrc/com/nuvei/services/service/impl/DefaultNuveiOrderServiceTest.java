package com.nuvei.services.service.impl;

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.user.EmployeeModel;
import de.hybris.platform.ordercancel.OrderCancelException;
import de.hybris.platform.ordercancel.OrderCancelService;
import de.hybris.platform.ordercancel.model.OrderCancelRecordEntryModel;
import de.hybris.platform.servicelayer.internal.dao.GenericDao;
import de.hybris.platform.servicelayer.user.UserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class DefaultNuveiOrderServiceTest {

    private static final String CLIENT_UNIQUE_ID = "clientUniqueId";

    @InjectMocks
    private DefaultNuveiOrderService testObj;

    @Mock
    private GenericDao<AbstractOrderModel> abstractOrderGenericDao;
    @Mock
    private OrderCancelService orderCancelServiceMock;
    @Mock
    private UserService userServiceMock;

    @Mock
    private AbstractOrderModel abstractOrderModelOneMock, abstractOrderModelTwoMock;
    @Mock
    private OrderCancelRecordEntryModel orderCancelRecordEntryModelMock;
    @Mock
    private EmployeeModel employeeModelMock;

    private final OrderModel orderModelStub = new OrderModel();
    private final OrderEntryModel orderEntryModelStub = new OrderEntryModel();

    @Test
    public void findAbstractOrderModelByClientUniqueId_shouldReturnAbstractOrderModel_whenOrderHasClientUniqueId() {
        when(abstractOrderGenericDao.find(Collections.singletonMap(AbstractOrderModel.CLIENTUNIQUEID, CLIENT_UNIQUE_ID))).thenReturn(Collections.singletonList(abstractOrderModelOneMock));

        final AbstractOrderModel result = testObj.findAbstractOrderModelByClientUniqueId(CLIENT_UNIQUE_ID);

        assertThat(result).isEqualTo(abstractOrderModelOneMock);
    }

    @Test
    public void findAbstractOrderModelByClientUniqueId_shouldReturnNull_whenOrderHasNoClientUniqueId() {
        when(abstractOrderGenericDao.find(Collections.singletonMap(AbstractOrderModel.CLIENTUNIQUEID, CLIENT_UNIQUE_ID))).thenReturn(Collections.EMPTY_LIST);

        final AbstractOrderModel result = testObj.findAbstractOrderModelByClientUniqueId(CLIENT_UNIQUE_ID);

        assertThat(result).isNull();
    }

    @Test
    public void findAbstractOrderModelByClientUniqueId_shouldReturnAbstractOrderModel_whenClientUniqueIdIsNotUnique() {
        when(abstractOrderGenericDao.find(Collections.singletonMap(AbstractOrderModel.CLIENTUNIQUEID, CLIENT_UNIQUE_ID))).thenReturn(List.of(abstractOrderModelOneMock, abstractOrderModelTwoMock));

        final AbstractOrderModel result = testObj.findAbstractOrderModelByClientUniqueId(CLIENT_UNIQUE_ID);

        assertThat(result).isEqualTo(abstractOrderModelOneMock);
    }

    @Test
    public void requestCancelOrder_shouldReturnTrue_whenRequestIsCreated() {
        when(userServiceMock.getAdminUser()).thenReturn(employeeModelMock);

        orderEntryModelStub.setQuantity(1L);
        orderEntryModelStub.setOrder(orderModelStub);
        orderModelStub.setEntries(Collections.singletonList(orderEntryModelStub));

        final Boolean result = testObj.requestCancelOrder(orderModelStub);

        assertThat(result).isTrue();
    }

    @Test
    public void requestCancelOrder_shouldReturnFalse_whenRequestThrowException() throws OrderCancelException {
        when(userServiceMock.getAdminUser()).thenReturn(employeeModelMock);
        doThrow(OrderCancelException.class).when(orderCancelServiceMock).requestOrderCancel(any(), any());

        orderEntryModelStub.setQuantity(1L);
        orderEntryModelStub.setOrder(orderModelStub);
        orderModelStub.setEntries(Collections.singletonList(orderEntryModelStub));

        final Boolean result = testObj.requestCancelOrder(orderModelStub);

        assertThat(result).isFalse();
    }
}
