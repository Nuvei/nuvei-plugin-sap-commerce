package com.nuvei.facades.openorder.impl;

import com.nuvei.services.openorder.NuveiOpenOrderService;
import com.safecharge.exception.SafechargeException;
import de.hybris.bootstrap.annotations.UnitTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class DefaultNuveiOpenOrderFacadeTest {

    private static final String SESSION_TOKEN = "SessionToken";

    @InjectMocks
    private DefaultNuveiOpenOrderFacade testObj;

    @Mock
    private NuveiOpenOrderService nuveiOpenOrderServiceMock;

    @Test
    public void requestOpenOrder_ShouldReturnSessionToken_WhenRequestIsSuccess() throws SafechargeException {
        when(nuveiOpenOrderServiceMock.requestOpenOrder()).thenReturn(SESSION_TOKEN);

        final String result = testObj.requestOpenOrder();

        assertThat(result).isEqualTo(SESSION_TOKEN);
    }

    @Test(expected = SafechargeException.class)
    public void requestOpenOrder_ShouldThrowException_WhenRequestIsNotSuccess() throws SafechargeException {
        when(nuveiOpenOrderServiceMock.requestOpenOrder()).thenThrow(SafechargeException.class);

        testObj.requestOpenOrder();
    }
}
