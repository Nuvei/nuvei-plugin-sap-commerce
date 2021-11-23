package com.nuvei.notifications.facades;

import com.nuvei.notifications.data.NuveiIncomingDMNData;
import com.nuvei.notifications.model.NuveiDirectMerchantNotificationModel;

/**
 * Interface to handle direct merchant notifications
 */
public interface NuveiDMNFacade {
    /**
     * Create a {@link NuveiDirectMerchantNotificationModel} from data in {@link NuveiIncomingDMNData} and save it.
     *
     * @param nuveiIncomingDMNData the notification received
     * @return {@link NuveiDirectMerchantNotificationModel}
     */
    NuveiDirectMerchantNotificationModel createAndSaveDMN(NuveiIncomingDMNData nuveiIncomingDMNData);
}
