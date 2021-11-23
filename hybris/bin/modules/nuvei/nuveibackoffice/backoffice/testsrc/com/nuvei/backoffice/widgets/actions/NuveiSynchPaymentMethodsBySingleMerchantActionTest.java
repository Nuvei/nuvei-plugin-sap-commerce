package com.nuvei.backoffice.widgets.actions;

import com.hybris.cockpitng.actions.ActionContext;
import com.hybris.cockpitng.actions.ActionResult;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class NuveiSynchPaymentMethodsBySingleMerchantActionTest {

    @Spy
    @InjectMocks
    private NuveiSynchPaymentMethodsBySingleMerchantAction testObj;

    @Mock
    private NuveiFilteringPaymentMethodsService nuveiPaymentMethodsServiceMock;

    @Mock
    private ActionContext<NuveiMerchantConfigurationModel> actionContext;
    @Mock
    private NuveiMerchantConfigurationModel firstNuveiMerchantInfoConfigurationModelMock;

    @Test
    public void perform_ShouldCallNuveiPaymentMethodsService_WhenOneItemsIsSelected() throws SafechargeException {
        doNothing().when(testObj).showMessage(actionContext);
        when(actionContext.getData()).thenReturn(firstNuveiMerchantInfoConfigurationModelMock);

        final ActionResult<String> result = testObj.perform(actionContext);

        verify(nuveiPaymentMethodsServiceMock).synchFilteringPaymentMethodsForMerchant(firstNuveiMerchantInfoConfigurationModelMock);
        assertThat(result.getResultCode()).isEqualTo(ActionResult.SUCCESS);
    }

    @Test
    public void perform_ShouldShowErrorMessage_WhenExceptionIsThrown() throws SafechargeException {
        doNothing().when(testObj).showErrorMessage(actionContext);

        doThrow(Exception.class).when(nuveiPaymentMethodsServiceMock).synchFilteringPaymentMethodsForMerchant(firstNuveiMerchantInfoConfigurationModelMock);

        final ActionResult<String> result = testObj.perform(actionContext);

        assertThat(result.getResultCode()).isEqualTo(ActionResult.ERROR);
    }

    @Test
    public void canPerform_ShouldReturnTrue() {
        when(actionContext.getData()).thenReturn(firstNuveiMerchantInfoConfigurationModelMock);

        final boolean result = testObj.canPerform(actionContext);

        assertThat(result).isTrue();
    }

    @Test
    public void needsConfirmation_ShouldReturnFalse() {
        final boolean result = testObj.needsConfirmation(actionContext);

        assertThat(result).isFalse();
    }

    @Test
    public void getConfirmationMessage_ShouldReturnNull() {
        final String result = testObj.getConfirmationMessage(actionContext);

        assertThat(result).isNull();
    }
}
