package com.nuvei.notifications.core.services.impl;

import com.nuvei.notifications.core.services.NuveiDMNService;
import com.nuvei.notifications.dao.NuveiDMNDao;
import com.nuvei.notifications.model.NuveiDirectMerchantNotificationModel;
import com.nuvei.services.enums.NuveiTransactionType;
import de.hybris.platform.servicelayer.exceptions.AmbiguousIdentifierException;
import org.apache.commons.collections4.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;

/**
 * Default implementation of {@link NuveiDMNService}
 */
public class DefaultNuveiDMNService implements NuveiDMNService {

    protected final NuveiDMNDao nuveiDMNDao;

    public DefaultNuveiDMNService(final NuveiDMNDao nuveiDMNDao) {
        this.nuveiDMNDao = nuveiDMNDao;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<NuveiDirectMerchantNotificationModel> getUnprocessedDMNsByType(final NuveiTransactionType nuveiTransactionType) {
        final Map<String, Object> objectMap = new HashMap<>();
        objectMap.put(NuveiDirectMerchantNotificationModel.PROCESSED, Boolean.FALSE);
        objectMap.put(NuveiDirectMerchantNotificationModel.TRANSACTIONTYPE, nuveiTransactionType);

        return nuveiDMNDao.find(objectMap);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NuveiDirectMerchantNotificationModel findDMNByTypePPPTransactionId(final String pppTransactionId) {
        final Map<String, Object> objectMap = new HashMap<>();
        objectMap.put(NuveiDirectMerchantNotificationModel.PPPTRANSACTIONID, pppTransactionId);

        final List<NuveiDirectMerchantNotificationModel> nuveiDirectMerchantNotificationModels = nuveiDMNDao.find(objectMap);

        if (nuveiDirectMerchantNotificationModels.size() > 1) {
            throw new AmbiguousIdentifierException(format("DirectMerchantNotification pppTransactionId '%s' is not unique, %d DirectMerchantNotifications found!", pppTransactionId,
                    nuveiDirectMerchantNotificationModels.size()));
        } else if (CollectionUtils.isEmpty(nuveiDirectMerchantNotificationModels)) {
            return null;
        }

        return nuveiDirectMerchantNotificationModels.get(0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<NuveiDirectMerchantNotificationModel> findDMNToDelete() {
        return nuveiDMNDao.findDMNToDelete();
    }
}
