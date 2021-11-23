package com.nuvei.services.jobs;

import com.nuvei.services.merchant.NuveiMerchantConfigurationService;
import com.nuvei.services.model.NuveiMerchantConfigurationModel;
import com.nuvei.services.model.NuveiMerchantSyncCronjobModel;
import com.nuvei.services.payments.NuveiFilteringPaymentMethodsService;
import com.safecharge.exception.SafechargeException;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.servicelayer.cronjob.PerformResult;
import org.assertj.core.util.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class NuveiPaymentMethodSyncJobTest {

    @InjectMocks
    private NuveiPaymentMethodSyncJob testObj;
    @Mock
    private NuveiMerchantSyncCronjobModel nuveiMerchantSyncCronjobModelMock;
    @Mock
    private NuveiMerchantConfigurationService nuveiMerchantConfigurationServiceMock;
    @Mock
    private NuveiMerchantConfigurationModel merchantConfigurationMock;
    @Mock
    private NuveiMerchantConfigurationModel secondMerchantConfigurationMock;
    @Mock
    private NuveiFilteringPaymentMethodsService nuveiFilteringPaymentMethodsServiceMock;

    @Test
    public void perform_ShouldRetrieveAllConfigurationsAndFinishedSuccees_WhenTheMerchantsConfigurationsAreNull()
            throws SafechargeException {
        when(nuveiMerchantSyncCronjobModelMock.getNuveiMerchantConfigurations()).thenReturn(null);
        when(nuveiMerchantConfigurationServiceMock.getAllMerchantConfigurations())
                .thenReturn(List.of(merchantConfigurationMock, secondMerchantConfigurationMock));

        final PerformResult result = testObj.perform(nuveiMerchantSyncCronjobModelMock);

        verify(nuveiFilteringPaymentMethodsServiceMock).synchFilteringPaymentMethodsForMerchant(merchantConfigurationMock);
        verify(nuveiFilteringPaymentMethodsServiceMock).synchFilteringPaymentMethodsForMerchant(secondMerchantConfigurationMock);

        assertThat(result.getResult()).isEqualTo(CronJobResult.SUCCESS);
        assertThat(result.getStatus()).isEqualTo(CronJobStatus.FINISHED);
    }


    @Test
    public void perform_ShouldRetrieveAllConfigurationsAndFinishedSuccees_WhenTheMerchantsConfigurationsAreEmpty()
            throws SafechargeException {
        when(nuveiMerchantSyncCronjobModelMock.getNuveiMerchantConfigurations()).thenReturn(Lists.emptyList());
        when(nuveiMerchantConfigurationServiceMock.getAllMerchantConfigurations())
                .thenReturn(List.of(merchantConfigurationMock, secondMerchantConfigurationMock));

        final PerformResult result = testObj.perform(nuveiMerchantSyncCronjobModelMock);

        verify(nuveiFilteringPaymentMethodsServiceMock).synchFilteringPaymentMethodsForMerchant(merchantConfigurationMock);
        verify(nuveiFilteringPaymentMethodsServiceMock).synchFilteringPaymentMethodsForMerchant(secondMerchantConfigurationMock);

        assertThat(result.getResult()).isEqualTo(CronJobResult.SUCCESS);
        assertThat(result.getStatus()).isEqualTo(CronJobStatus.FINISHED);
    }

    @Test
    public void perform_ShouldProcessMerchantConfigurationsAndFinishedSuccees_WhenTheMerchantsConfigurationsAreNotEmpty()
            throws SafechargeException {
        when(nuveiMerchantSyncCronjobModelMock.getNuveiMerchantConfigurations()).thenReturn(List.of(merchantConfigurationMock));

        final PerformResult result = testObj.perform(nuveiMerchantSyncCronjobModelMock);

        verify(nuveiFilteringPaymentMethodsServiceMock).synchFilteringPaymentMethodsForMerchant(merchantConfigurationMock);

        assertThat(result.getResult()).isEqualTo(CronJobResult.SUCCESS);
        assertThat(result.getStatus()).isEqualTo(CronJobStatus.FINISHED);
    }

    @Test
    public void perform_ShouldProcessCorrectMerchantConfigurationsAndFinishedSuccees_WhenSomeMerchantProcessingFails()
            throws SafechargeException {
        when(nuveiMerchantSyncCronjobModelMock.getNuveiMerchantConfigurations()).thenReturn(List.of(merchantConfigurationMock));

        final PerformResult result = testObj.perform(nuveiMerchantSyncCronjobModelMock);

        doThrow(SafechargeException.class)
                .when(nuveiFilteringPaymentMethodsServiceMock).synchFilteringPaymentMethodsForMerchant(merchantConfigurationMock);

        assertThat(result.getResult()).isEqualTo(CronJobResult.SUCCESS);
        assertThat(result.getStatus()).isEqualTo(CronJobStatus.FINISHED);
    }

    @Test
    public void processMerchants_ShouldCatchExceptions() throws SafechargeException {
        doThrow(SafechargeException.class)
                .when(nuveiFilteringPaymentMethodsServiceMock)
                .synchFilteringPaymentMethodsForMerchant(merchantConfigurationMock);

        testObj.processMerchants(List.of(merchantConfigurationMock));
    }
}
