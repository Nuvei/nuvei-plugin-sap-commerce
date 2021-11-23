package com.nuvei.notifications.jobs;

import com.nuvei.notifications.core.services.NuveiDMNService;
import com.nuvei.notifications.dao.NuveiDMNDao;
import com.nuvei.notifications.model.NuveiDirectMerchantNotificationModel;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.cronjob.model.CronJobModel;
import de.hybris.platform.servicelayer.cronjob.PerformResult;
import de.hybris.platform.servicelayer.model.ModelService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;


@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class NuveiCleanOldNotificationsJobTest {

    @InjectMocks
    private NuveiCleanOldNotificationsJob testObj;

    @Mock
    private NuveiDMNDao nuveiDMNDaoMock;
    @Mock
    private ModelService modelServiceMock;
    @Mock
    private NuveiDMNService nuveiDMNServiceMock;

    private final CronJobModel cronJobModelStub = new CronJobModel();
    private final NuveiDirectMerchantNotificationModel firstNuveiDMN = new NuveiDirectMerchantNotificationModel();
    private final NuveiDirectMerchantNotificationModel secondNuveiDMN = new NuveiDirectMerchantNotificationModel();

    @Before
    public void setUp() throws Exception {
        this.testObj.setModelService(modelServiceMock);
    }

    @Test
    public void perform_ShouldRemoveAllOldProcessedDMNs_WhenThereAreOldProcessedDMNs() {
        when(nuveiDMNServiceMock.findDMNToDelete()).thenReturn(List.of(firstNuveiDMN, secondNuveiDMN));

        final PerformResult result = testObj.perform(cronJobModelStub);

        verify(modelServiceMock).removeAll(List.of(firstNuveiDMN, secondNuveiDMN));
        assertThat(result.getResult()).isEqualTo(CronJobResult.SUCCESS);
        assertThat(result.getStatus()).isEqualTo(CronJobStatus.FINISHED);
    }

    @Test
    public void perform_ShouldNotCallModelService_WhenThereAreNotProcessedDMNs() {
        when(nuveiDMNServiceMock.findDMNToDelete()).thenReturn(Collections.emptyList());

        final PerformResult result = testObj.perform(cronJobModelStub);

        verifyZeroInteractions(modelServiceMock);
        assertThat(result.getResult()).isEqualTo(CronJobResult.SUCCESS);
        assertThat(result.getStatus()).isEqualTo(CronJobStatus.FINISHED);
    }
}
