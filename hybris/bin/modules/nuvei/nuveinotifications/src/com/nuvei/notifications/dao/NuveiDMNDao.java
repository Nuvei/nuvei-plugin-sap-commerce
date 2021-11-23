package com.nuvei.notifications.dao;

import com.nuvei.notifications.model.NuveiDirectMerchantNotificationModel;
import de.hybris.platform.servicelayer.internal.dao.GenericDao;

import java.util.List;

/**
 * Nuvei Direct Merchant Notifications Dao
 */
public interface NuveiDMNDao extends GenericDao<NuveiDirectMerchantNotificationModel> {

    /**
     * Find all the Nuvei Direct Merchant Notifications that has been processed and las modified time is before than
     * the date minus an amount of days configured via property 'nuvei.clean.notifications.job.days'
     *
     * @return a list with the Nuvei Direct Merchant Notifications
     */
    List<NuveiDirectMerchantNotificationModel> findDMNToDelete();
}
