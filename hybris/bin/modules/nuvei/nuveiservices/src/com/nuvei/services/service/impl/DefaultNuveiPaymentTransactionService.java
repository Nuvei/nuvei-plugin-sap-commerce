package com.nuvei.services.service.impl;

import com.nuvei.notifications.model.NuveiDirectMerchantNotificationModel;
import com.nuvei.services.enums.NuveiTransactionType;
import com.nuvei.services.service.NuveiPaymentTransactionService;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.order.payment.PaymentInfoModel;
import de.hybris.platform.payment.enums.PaymentTransactionType;
import de.hybris.platform.payment.model.PaymentTransactionEntryModel;
import de.hybris.platform.payment.model.PaymentTransactionModel;
import de.hybris.platform.servicelayer.internal.dao.GenericDao;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.time.TimeService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;

/**
 * Default implementation of {@link NuveiPaymentTransactionService}
 */
public class DefaultNuveiPaymentTransactionService implements NuveiPaymentTransactionService {

    protected static final Logger LOG = LogManager.getLogger(DefaultNuveiPaymentTransactionService.class);

    protected static final String ORDER_MODEL_CANNOT_BE_NULL = "OrderModel cannot be null";
    protected static final String ORDER_DOES_NOT_HAVE_ANY_PAYMENT_TRANSACTION = "Order does not have any payment transaction.";
    protected static final String PAYMENT_TRANSACTION_PROVIDER = "NUVEI";
    private static final String SEPARATOR = "_";

    protected final ModelService modelService;
    protected final TimeService timeService;
    protected final GenericDao<PaymentTransactionEntryModel> paymentTransactionEntryModelGenericDao;

    public DefaultNuveiPaymentTransactionService(final ModelService modelService, final TimeService timeService, final GenericDao<PaymentTransactionEntryModel> paymentTransactionEntryModelGenericDao) {
        this.modelService = modelService;
        this.timeService = timeService;
        this.paymentTransactionEntryModelGenericDao = paymentTransactionEntryModelGenericDao;
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    public PaymentTransactionEntryModel createPaymentTransactionEntry(final PaymentTransactionModel paymentTransaction,
                                                                      final NuveiDirectMerchantNotificationModel notificationModel,
                                                                      final PaymentTransactionType paymentTransactionType) {
        final List<PaymentTransactionEntryModel> paymentTransactionEntryModels = paymentTransactionEntryModelGenericDao.find(Map.of(PaymentTransactionEntryModel.NUVEINOTIFICATIONID, notificationModel.getId()));

        if (CollectionUtils.isNotEmpty(paymentTransactionEntryModels)) {
            LOG.warn("Payment transaction entry already exists for notification with id {} for order {}, avoiding the creation",
                    notificationModel.getId(), paymentTransaction.getOrder().getCode());
            return paymentTransactionEntryModels.get(0);
        }

        final PaymentTransactionEntryModel transactionEntryModel = modelService.create(PaymentTransactionEntryModel.class);
        transactionEntryModel.setNuveiNotificationId(notificationModel.getId());
        transactionEntryModel.setCode(getNewPaymentTransactionEntryCode(paymentTransaction, paymentTransactionType));
        transactionEntryModel.setRequestId(notificationModel.getTransactionId());
        transactionEntryModel.setType(paymentTransactionType);
        transactionEntryModel.setPaymentTransaction(paymentTransaction);
        transactionEntryModel.setTime(timeService.getCurrentTime());
        Optional.ofNullable(notificationModel.getStatus())
                .ifPresentOrElse(nuveiTransactionStatus -> transactionEntryModel.setTransactionStatus(nuveiTransactionStatus.getCode()),
                        () -> LOG.warn("There is no transactionStatus on DMN with id {}", notificationModel.getId()));
        Optional.ofNullable(notificationModel.getPppStatus()).ifPresentOrElse(nuveiPPPTransactionStatus -> transactionEntryModel.setTransactionStatusDetails(nuveiPPPTransactionStatus.getCode()),
                () -> LOG.warn("There is no PPPTransactionStatus on DMN with id {}", notificationModel.getId()));
        transactionEntryModel.setTransactionStatusDetails(notificationModel.getPppStatus().getCode());
        transactionEntryModel.setAmount(notificationModel.getTotalAmount());
        transactionEntryModel.setCurrency(notificationModel.getCurrency());
        transactionEntryModel.setClientRequestId(notificationModel.getClientRequestId());
        transactionEntryModel.setFinalFraudDecision(notificationModel.getFinalFraudDecision());
        transactionEntryModel.setSystemDecision(notificationModel.getSystemDecision());

        modelService.save(transactionEntryModel);
        modelService.refresh(paymentTransaction);

        LOG.info("Payment transaction entry for DMN with id {} created for order {}", transactionEntryModel.getCode(),
                paymentTransaction.getOrder().getCode());

        return transactionEntryModel;
    }

    @Override
    public PaymentTransactionEntryModel createPaymentTransactionEntry(final PaymentTransactionModel paymentTransactionModel,
                                                                      final String status, final PaymentTransactionType paymentTransactionType) {

        final PaymentTransactionEntryModel transactionEntryModel = modelService.create(PaymentTransactionEntryModel.class);
        final Date currentTime = timeService.getCurrentTime();
        final String code = paymentTransactionModel.getOrder().getCode() + SEPARATOR + status
                + SEPARATOR + currentTime.getTime();
        transactionEntryModel.setCode(code);
        transactionEntryModel.setRequestId(code);
        transactionEntryModel.setType(paymentTransactionType);
        transactionEntryModel.setTransactionStatus(status);
        transactionEntryModel.setPaymentTransaction(paymentTransactionModel);
        transactionEntryModel.setTime(currentTime);
        transactionEntryModel.setAmount(BigDecimal.valueOf(paymentTransactionModel.getOrder().getTotalPrice()));
        transactionEntryModel.setCurrency(paymentTransactionModel.getOrder().getCurrency());

        modelService.save(transactionEntryModel);
        modelService.refresh(paymentTransactionModel);
        return transactionEntryModel;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PaymentTransactionModel findOrCreatePaymentTransaction(final NuveiDirectMerchantNotificationModel notificationModel, final OrderModel order, final NuveiTransactionType nuveiTransactionType) {
        if (CollectionUtils.isEmpty(order.getPaymentTransactions())) {
            LOG.debug("Creating a new transaction for notification with id [{}] and order [{}]", notificationModel.getPppTransactionId(), order.getCode());
            return createPaymentTransaction(notificationModel, order);
        } else {
            LOG.debug("Getting existing transaction for notification with id [{}] and order [{}]", notificationModel.getPppTransactionId(), order.getCode());
            return getPaymentTransaction(order);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PaymentTransactionModel getPaymentTransaction(final OrderModel order) {
        validateParameterNotNull(order, ORDER_MODEL_CANNOT_BE_NULL);
        checkArgument(CollectionUtils.isNotEmpty(order.getPaymentTransactions()), ORDER_DOES_NOT_HAVE_ANY_PAYMENT_TRANSACTION);

        if (order.getPaymentTransactions().size() > 1) {
            LOG.warn("Found [{}] payment transactions for order: [{}]", order.getPaymentTransactions().size(), order.getCode());
        }
        return order.getPaymentTransactions().get(0);
    }

    /**
     * Creates a new paymentTransaction on the order
     *
     * @param notificationModel the nuvei direct merchant notification
     * @param order             the order model
     * @return the created {@link PaymentTransactionModel}
     */
    protected PaymentTransactionModel createPaymentTransaction(final NuveiDirectMerchantNotificationModel notificationModel,
                                                               final OrderModel order) {
        validateParameterNotNull(notificationModel, "Payment identifier cannot be null");
        validateParameterNotNull(order, ORDER_MODEL_CANNOT_BE_NULL);

        final PaymentTransactionModel paymentTransactionModel = modelService.create(PaymentTransactionModel.class);
        final PaymentInfoModel paymentInfo = order.getPaymentInfo();

        paymentTransactionModel.setCode(notificationModel.getTransactionId());
        paymentTransactionModel.setRequestId(notificationModel.getTransactionId());
        paymentTransactionModel.setRequestToken(notificationModel.getMerchantId());
        paymentTransactionModel.setPaymentProvider(PAYMENT_TRANSACTION_PROVIDER);
        paymentTransactionModel.setOrder(order);
        paymentTransactionModel.setCurrency(order.getCurrency());
        paymentTransactionModel.setInfo(paymentInfo);
        Optional.ofNullable(order.getTotalPrice())
                .map(BigDecimal::new)
                .ifPresent(paymentTransactionModel::setPlannedAmount);

        modelService.save(paymentTransactionModel);
        modelService.refresh(order);

        return paymentTransactionModel;
    }

    /**
     * Create code for {@link PaymentTransactionEntryModel}
     *
     * @param transaction
     * @param paymentTransactionType
     * @return string with the code
     */
    protected String getNewPaymentTransactionEntryCode(final PaymentTransactionModel transaction, final PaymentTransactionType paymentTransactionType) {
        return transaction.getEntries() == null ? transaction.getCode() + "-" + paymentTransactionType.getCode() + "-1" : transaction.getCode() + "-" + paymentTransactionType.getCode() + "-" + (transaction.getEntries().size() + 1);
    }
}
