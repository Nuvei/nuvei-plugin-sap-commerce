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

import java.util.LinkedHashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class NuveiSynchPaymentMethodsByMerchantsActionTest {

    @Spy
    @InjectMocks
    private NuveiSynchPaymentMethodsByMerchantsAction testObj;

    @Mock
    private NuveiFilteringPaymentMethodsService nuveiPaymentMethodsServiceMock;

    @Mock
    private ActionContext<LinkedHashSet<NuveiMerchantConfigurationModel>> actionContext;
    @Mock
    private NuveiMerchantConfigurationModel firstNuveiMerchantInfoConfigurationModelMock, secondNuveiMerchantInfoConfigurationModelMock;

    @Test
    public void Perform_ShouldCallNuveiMerchantConfigurationServiceAnd_NuveiPaymentMethodsService() throws SafechargeException {
        doNothing().when(testObj).showMessage(actionContext);

        final LinkedHashSet<NuveiMerchantConfigurationModel> linkedHashSet = new LinkedHashSet<>();
        linkedHashSet.add(firstNuveiMerchantInfoConfigurationModelMock);
        linkedHashSet.add(secondNuveiMerchantInfoConfigurationModelMock);

        when(actionContext.getData()).thenReturn(linkedHashSet);

        final ActionResult<String> result = testObj.perform(actionContext);

        verify(nuveiPaymentMethodsServiceMock).synchFilteringPaymentMethodsForMerchant(firstNuveiMerchantInfoConfigurationModelMock);
        verify(nuveiPaymentMethodsServiceMock).synchFilteringPaymentMethodsForMerchant(secondNuveiMerchantInfoConfigurationModelMock);
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
    public void canPerform_ShouldReturnFalse_WhenCollectionIsEmpty() {
        when(actionContext.getData()).thenReturn(new LinkedHashSet<>());

        final boolean result = testObj.canPerform(actionContext);

        assertThat(result).isFalse();
    }

    @Test
    public void canPerform_ShouldReturnTrue_WhenDataIsCollectionOfNuveiMerchantInfoConfigurationModel() {
        when(actionContext.getData()).thenReturn(new LinkedHashSet<>(List.of(firstNuveiMerchantInfoConfigurationModelMock, secondNuveiMerchantInfoConfigurationModelMock)));

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
