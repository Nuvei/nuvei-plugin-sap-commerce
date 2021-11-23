package com.nuvei.notifications.jobs;

import com.nuvei.notifications.core.services.NuveiNotificationsProcessService;
import com.nuvei.notifications.model.NuveiProcessNotificationsCronjobModel;
import com.nuvei.services.enums.NuveiTransactionType;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.servicelayer.cronjob.PerformResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class NuveiProcessNotificationsJobTest {

    @InjectMocks
    private NuveiProcessNotificationsJob testObj;

    @Mock
    private NuveiNotificationsProcessService nuveiNotificationsProcessServiceMock;

    private final NuveiProcessNotificationsCronjobModel nuveiProcessNotificationsCronjobModel = new NuveiProcessNotificationsCronjobModel();


    @Test
    public void perform_ShouldProcessAllNuveiTransactionTypesAndFinishedSuccess_WhenTheNuveiTransactionTypesAreNull() {
        final PerformResult result = testObj.perform(nuveiProcessNotificationsCronjobModel);

        verify(nuveiNotificationsProcessServiceMock).processNuveiNotifications(NuveiTransactionType.AUTH);
        verify(nuveiNotificationsProcessServiceMock).processNuveiNotifications(NuveiTransactionType.CHARGEBACK);
        verify(nuveiNotificationsProcessServiceMock).processNuveiNotifications(NuveiTransactionType.CREDIT);
        verify(nuveiNotificationsProcessServiceMock).processNuveiNotifications(NuveiTransactionType.MODIFICATION);
        verify(nuveiNotificationsProcessServiceMock).processNuveiNotifications(NuveiTransactionType.SALE);
        verify(nuveiNotificationsProcessServiceMock).processNuveiNotifications(NuveiTransactionType.VOID);

        assertThat(result.getResult()).isEqualTo(CronJobResult.SUCCESS);
        assertThat(result.getStatus()).isEqualTo(CronJobStatus.FINISHED);
    }

    @Test
    public void perform_ShouldProcessGivenlNuveiTransactionTypesAndFinishedSuccess_WhenTheNuveiTransactionTypesAreNotEmpty() {
        nuveiProcessNotificationsCronjobModel.setNuveiNotificationTypes(List.of(NuveiTransactionType.AUTH, NuveiTransactionType.CREDIT));

        final PerformResult result = testObj.perform(nuveiProcessNotificationsCronjobModel);

        verify(nuveiNotificationsProcessServiceMock).processNuveiNotifications(NuveiTransactionType.AUTH);
        verify(nuveiNotificationsProcessServiceMock).processNuveiNotifications(NuveiTransactionType.CREDIT);

        assertThat(result.getResult()).isEqualTo(CronJobResult.SUCCESS);
        assertThat(result.getStatus()).isEqualTo(CronJobStatus.FINISHED);
    }

}
