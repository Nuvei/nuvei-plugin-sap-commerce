package com.nuvei.services.service.impl;

import com.nuvei.services.model.NuveiPaymentMethodModel;
import com.nuvei.services.service.NuveiPaymentMethodService;
import de.hybris.platform.servicelayer.internal.dao.GenericDao;

import java.util.Collections;

/**
 * Default implementation of {@link NuveiPaymentMethodService}
 */
public class DefaultNuveiPaymentMethodService implements NuveiPaymentMethodService {

    protected final GenericDao<NuveiPaymentMethodModel> nuveiPaymentMethodGenericDao;

    /**
     * Default constructor {@link NuveiPaymentMethodService}
     *
     * @param nuveiPaymentMethodGenericDao
     */
    public DefaultNuveiPaymentMethodService(final GenericDao<NuveiPaymentMethodModel> nuveiPaymentMethodGenericDao) {
        this.nuveiPaymentMethodGenericDao = nuveiPaymentMethodGenericDao;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NuveiPaymentMethodModel findNuveiPaymentMethodById(final String id) {
        return nuveiPaymentMethodGenericDao.find(Collections.singletonMap(NuveiPaymentMethodModel.ID, id))
                .stream()
                .findAny()
                .orElse(null);
    }
}
