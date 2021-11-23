package com.nuvei.notifications.dao.impl;

import com.nuvei.notifications.dao.NuveiDMNDao;
import com.nuvei.notifications.model.NuveiDirectMerchantNotificationModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.internal.dao.DefaultGenericDao;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.SearchResult;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

/**
 * {@inheritDoc}
 */
public class DefaultNuveiDMNDao extends DefaultGenericDao<NuveiDirectMerchantNotificationModel> implements NuveiDMNDao {

    private static final String NUVEI_CLEAN_JOB_DAYS = "nuvei.clean.notifications.job.days";
    private static final String DATE_PARAMETER = "date";

    protected static final String NUVEI_FIND_OLD_DMN = "SELECT {" + NuveiDirectMerchantNotificationModel.PK + "} " +
            "FROM {" + NuveiDirectMerchantNotificationModel._TYPECODE + "} " +
            "WHERE {" + NuveiDirectMerchantNotificationModel.PROCESSED + "}  = ?" + NuveiDirectMerchantNotificationModel.PROCESSED + " " +
            "AND {" + NuveiDirectMerchantNotificationModel.MODIFIEDTIME + "}  < ?" + DATE_PARAMETER + " ";

    private ConfigurationService configurationService;

    public DefaultNuveiDMNDao(){
        super(NuveiDirectMerchantNotificationModel._TYPECODE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<NuveiDirectMerchantNotificationModel> findDMNToDelete() {
        final int days = configurationService.getConfiguration().getInt(NUVEI_CLEAN_JOB_DAYS);
        final Date date = Date.from(LocalDate.now().minusDays(days).atStartOfDay(ZoneId.systemDefault()).toInstant());
        final FlexibleSearchQuery query = new FlexibleSearchQuery(NUVEI_FIND_OLD_DMN);
        query.addQueryParameter(NuveiDirectMerchantNotificationModel.PROCESSED, true);
        query.addQueryParameter(DATE_PARAMETER, date);
        final SearchResult<NuveiDirectMerchantNotificationModel> searchResult = getFlexibleSearchService().search(query);
        return searchResult.getResult();
    }

    public ConfigurationService getConfigurationService() {
        return configurationService;
    }

    public void setConfigurationService(final ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }
}
