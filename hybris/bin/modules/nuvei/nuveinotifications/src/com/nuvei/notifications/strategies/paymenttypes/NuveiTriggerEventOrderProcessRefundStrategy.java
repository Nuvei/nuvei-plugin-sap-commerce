package com.nuvei.notifications.strategies.paymenttypes;

import com.nuvei.notifications.dao.ProcessDefinitionDao;
import com.nuvei.notifications.enums.NuveiTransactionStatus;
import com.nuvei.notifications.strategies.NuveiAbstractTriggerEventOrderProcessStrategy;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.payment.enums.PaymentTransactionType;
import de.hybris.platform.payment.model.PaymentTransactionEntryModel;
import de.hybris.platform.payment.model.PaymentTransactionModel;
import de.hybris.platform.processengine.BusinessProcessEvent;
import de.hybris.platform.processengine.BusinessProcessService;
import de.hybris.platform.returns.model.ReturnProcessModel;
import de.hybris.platform.returns.model.ReturnRequestModel;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

/**
 * Triggers the events for the REFUND {@link PaymentTransactionType} transition
 */
public class NuveiTriggerEventOrderProcessRefundStrategy extends NuveiAbstractTriggerEventOrderProcessStrategy {
    private static final String SEPARATOR = "_";
    private static final Logger log = LogManager.getLogger(NuveiTriggerEventOrderProcessRefundStrategy.class);

    /**
     * {@inheritDoc}
     */
    protected NuveiTriggerEventOrderProcessRefundStrategy(final BusinessProcessService businessProcessService,
                                                          final ProcessDefinitionDao processDefinitionDao) {
        super(log, businessProcessService, processDefinitionDao);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean execute(final Pair<PaymentTransactionEntryModel, OrderModel> source) {
        log.info("Executing strategy  {}", NuveiTriggerEventOrderProcessRefundStrategy.class.getName());

        final ReturnRequestModel returnRequest = source.getRight().getReturnRequests().stream()
                .filter(requestModel -> requestModel.getCode().equals(source.getLeft().getClientRequestId()))
                .findAny()
                .orElse(null);

        if (returnRequest != null) {
            final ReturnProcessModel returnProcess = returnRequest.getReturnProcess().stream()
                    .filter(returnProcessModel -> returnProcessModel.getReturnRequest().getCode().equals(source.getLeft().getClientRequestId()))
                    .findAny()
                    .orElse(null);

            if (returnProcess != null) {
                final BusinessProcessEvent.Builder builder = BusinessProcessEvent.builder(returnProcess.getCode() + SEPARATOR + source.getLeft().getType().getCode());

                final BusinessProcessEvent event = builder.build();
                log.info("Triggering event {}", event);

                return businessProcessService.triggerEvent(event);
            }

        }

        return Boolean.FALSE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isApplicable(final Pair<PaymentTransactionEntryModel, OrderModel> source) {

        return Stream.ofNullable(source.getRight().getPaymentTransactions())
                .flatMap(Collection::stream)
                .map(PaymentTransactionModel::getEntries)
                .flatMap(Stream::ofNullable)
                .flatMap(List::stream)
                .anyMatch(entry -> isValidTransactionType(entry, source.getRight()));
    }

    /**
     * check if {@link PaymentTransactionEntryModel} has type {@code REFUND_FOLLOW_ON}, matches clientRequestId
     * and {@link OrderModel} has a {@link PaymentTransactionType} whit status {@code PENDING} and  type {@code REFUND_FOLLOW_ON}
     *
     * @param paymentTransactionEntryModel
     * @param orderModel
     * @return true if {@link PaymentTransactionEntryModel} has type {@code REFUND_FOLLOW_ON}, matches clientRequestId
     * and {@link OrderModel} has a {@link PaymentTransactionType} whit status {@code PENDING} and  type {@code REFUND_FOLLOW_ON}
     */
    protected boolean isValidTransactionType(final PaymentTransactionEntryModel paymentTransactionEntryModel,
                                             final OrderModel orderModel) {
        return PaymentTransactionType.REFUND_FOLLOW_ON.equals(paymentTransactionEntryModel.getType()) &&
                orderModel.getReturnRequests().stream()
                        .anyMatch(requestModel -> requestModel.getCode().
                                equals(paymentTransactionEntryModel.getClientRequestId())) &&
                orderModel.getPaymentTransactions().stream()
                        .flatMap(Stream::ofNullable)
                        .map(PaymentTransactionModel::getEntries)
                        .flatMap(Stream::ofNullable)
                        .flatMap(List::stream)
                        .anyMatch(entry -> PaymentTransactionType.REFUND_FOLLOW_ON.equals(entry.getType()) &&
                                NuveiTransactionStatus.PENDING.getCode().equals(entry.getTransactionStatus()) &&
                                entry.getClientRequestId().equals(paymentTransactionEntryModel.getClientRequestId()));
    }

}
