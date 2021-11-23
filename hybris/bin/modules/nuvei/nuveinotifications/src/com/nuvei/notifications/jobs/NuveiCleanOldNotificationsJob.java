package com.nuvei.notifications.jobs;

import com.nuvei.notifications.core.services.NuveiDMNService;
import com.nuvei.notifications.model.NuveiDirectMerchantNotificationModel;
import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.cronjob.model.CronJobModel;
import de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable;
import de.hybris.platform.servicelayer.cronjob.PerformResult;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;

/**
 * Job to clean the old Nuvei Direct Merchant Notifications
 */
public class NuveiCleanOldNotificationsJob extends AbstractJobPerformable<CronJobModel> {

    private final NuveiDMNService nuveiDMNService;

    /**
     * Default constructor for {@link NuveiCleanOldNotificationsJob}
     *
     * @param nuveiDMNService injected
     */
    public NuveiCleanOldNotificationsJob(final NuveiDMNService nuveiDMNService) {
        this.nuveiDMNService = nuveiDMNService;
    }

    /**
     * Perform the old Nuvei Direct Merchant Notifications clean
     *
     * @param cronJobModel the cronjob model
     * @return the performResult
     */
    @Override
    public PerformResult perform(final CronJobModel cronJobModel) {
        final List<NuveiDirectMerchantNotificationModel> dmnsToDelete = nuveiDMNService.findDMNToDelete();
        if (CollectionUtils.isNotEmpty(dmnsToDelete)) {
            modelService.removeAll(dmnsToDelete);
        }
        return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
    }
}
