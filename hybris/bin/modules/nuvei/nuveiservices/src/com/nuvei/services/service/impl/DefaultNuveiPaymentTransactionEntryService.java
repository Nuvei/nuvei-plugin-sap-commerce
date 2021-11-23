package com.nuvei.services.service.impl;

import com.nuvei.services.service.NuveiPaymentTransactionEntryService;
import de.hybris.platform.payment.enums.PaymentTransactionType;
import de.hybris.platform.payment.model.PaymentTransactionEntryModel;
import de.hybris.platform.servicelayer.internal.dao.DefaultGenericDao;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;

/**
 * Default implementation of {@link NuveiPaymentTransactionEntryService}
 */
public class DefaultNuveiPaymentTransactionEntryService implements NuveiPaymentTransactionEntryService {

    protected static final String REQUEST_ID_CANNOT_BE_NULL = "Request ID cannot be null";
    protected static final String CLIENT_REQUEST_ID_CANNOT_BE_NULL = "Client Request ID cannot be null";

    protected final DefaultGenericDao<PaymentTransactionEntryModel> paymentTransactionEntryGenericDao;

    /**
     * Default constructor {@link NuveiPaymentTransactionEntryService}
     *
     * @param paymentTransactionEntryGenericDao
     */
    public DefaultNuveiPaymentTransactionEntryService(final DefaultGenericDao<PaymentTransactionEntryModel> paymentTransactionEntryGenericDao) {
        this.paymentTransactionEntryGenericDao = paymentTransactionEntryGenericDao;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PaymentTransactionEntryModel findPaymentEntryByRequestIdAndType(final String requestId, final PaymentTransactionType type) {
        validateParameterNotNull(requestId, REQUEST_ID_CANNOT_BE_NULL);

        final List<PaymentTransactionEntryModel> paymentTransactionEntryModels =
                paymentTransactionEntryGenericDao.find(
                        Map.of(
                                PaymentTransactionEntryModel.REQUESTID, requestId,
                                PaymentTransactionEntryModel.TYPE, type
                        ));

        final List<PaymentTransactionEntryModel> filterEntries = paymentTransactionEntryModels.stream()
                .filter(entry -> StringUtils.isEmpty(entry.getVersionID()))
                .collect(Collectors.toList());

        return CollectionUtils.isNotEmpty(filterEntries) && filterEntries.size() == 1 ?
                filterEntries.get(0) : null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PaymentTransactionEntryModel findPaymentEntryByClientRequestIDAndType(final String clientRequestId, final PaymentTransactionType type) {
        validateParameterNotNull(clientRequestId, CLIENT_REQUEST_ID_CANNOT_BE_NULL);

        final List<PaymentTransactionEntryModel> paymentTransactionEntryModels =
                paymentTransactionEntryGenericDao.find(
                        Map.of(
                                PaymentTransactionEntryModel.CLIENTREQUESTID, clientRequestId,
                                PaymentTransactionEntryModel.TYPE, type
                        ));

        final List<PaymentTransactionEntryModel> filterEntries = paymentTransactionEntryModels.stream()
                .filter(entry -> StringUtils.isEmpty(entry.getVersionID()))
                .collect(Collectors.toList());

        return CollectionUtils.isNotEmpty(filterEntries) && filterEntries.size() == 1 ?
                filterEntries.get(0) : null;
    }
}
