package com.nuvei.backoffice.widgets.actions;

import com.hybris.cockpitng.actions.ActionContext;
import com.hybris.cockpitng.actions.ActionResult;
import com.nuvei.services.merchant.NuveiMerchantConfigurationService;
import com.nuvei.services.model.NuveiMerchantConfigurationModel;
import com.nuvei.services.payments.NuveiFilteringPaymentMethodsService;
import com.safecharge.exception.SafechargeException;
import de.hybris.bootstrap.annotations.UnitTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class NuveiSynchPaymentMethodsActionTest {

    @Spy
    @InjectMocks
    private NuveiSynchPaymentMethodsAction testObj;

    @Mock
    private NuveiFilteringPaymentMethodsService nuveiPaymentMethodsServiceMock;
    @Mock
    private NuveiMerchantConfigurationService nuveiMerchantConfigurationServiceMock;

    @Mock
    private ActionContext<Object> actionContextMock;
    @Mock
    private NuveiMerchantConfigurationModel firstNuveiMerchantConfigurationModelMock;
    @Mock
    private NuveiMerchantConfigurationModel secondNuveiMerchantConfigurationModelMock;

    @Test
    public void perform_ShouldCallNuveiPaymentMethodsService() throws SafechargeException {
        doNothing().when(testObj).showMessage(actionContextMock);
        when(nuveiMerchantConfigurationServiceMock.getAllMerchantConfigurations())
                .thenReturn(List.of(firstNuveiMerchantConfigurationModelMock, secondNuveiMerchantConfigurationModelMock));

        testObj.perform(actionContextMock);

        verify(nuveiMerchantConfigurationServiceMock).getAllMerchantConfigurations();
        verify(nuveiPaymentMethodsServiceMock).synchFilteringPaymentMethodsForMerchant(firstNuveiMerchantConfigurationModelMock);
        verify(nuveiPaymentMethodsServiceMock).synchFilteringPaymentMethodsForMerchant(secondNuveiMerchantConfigurationModelMock);
    }

    @Test
    public void perform_ShouldShowErrorMessage_WhenExceptionIsThrown() {
        doNothing().when(testObj).showErrorMessage(actionContextMock);
        doThrow(Exception.class).when(nuveiMerchantConfigurationServiceMock).getAllMerchantConfigurations();

        final ActionResult<Object> result = testObj.perform(actionContextMock);

        assertThat(result.getResultCode()).isEqualTo(ActionResult.ERROR);
    }

    @Test
    public void canPerform_ShouldReturnTrue() {
        final boolean result = testObj.canPerform(actionContextMock);

        assertThat(result).isTrue();
    }

    @Test
    public void needsConfirmation_ShouldReturnFalse() {
        final boolean result = testObj.needsConfirmation(actionContextMock);

        assertThat(result).isFalse();
    }

    @Test
    public void getConfirmationMessage_ShouldReturnNull() {
        final String result = testObj.getConfirmationMessage(actionContextMock);

        assertThat(result).isNull();
    }
}
