package com.nuvei.backoffice.widgets.actions;

import com.hybris.cockpitng.actions.ActionContext;
import com.hybris.cockpitng.actions.ActionResult;
import com.nuvei.notifications.dao.ProcessDefinitionDao;
import com.nuvei.notifications.enums.NuveiTransactionStatus;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.omsbackoffice.actions.order.ManualPaymentCaptureAction;
import de.hybris.platform.payment.enums.PaymentTransactionType;
import de.hybris.platform.payment.model.PaymentTransactionEntryModel;
import de.hybris.platform.payment.model.PaymentTransactionModel;
import de.hybris.platform.processengine.BusinessProcessEvent;
import de.hybris.platform.processengine.BusinessProcessService;
import de.hybris.platform.processengine.model.BusinessProcessModel;
import org.apache.commons.collections.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.zkoss.zul.Messagebox;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public class NuveiManualCapturePaymentAction extends ManualPaymentCaptureAction {

    private static final String SEPARATOR = "_";
    private static final String BACKOFFICE = "BACKOFFICE";
    private static final String NUVEI_ACTION_RESULT_MESSAGE_BOX_ERROR_TITLE = "nuvei.actionResult.messageBox.failed.title";
    private static final String NUVEI_ACTION_RESULT_MESSAGE_BOX_SUCCESS_TITLE = "nuvei.actionResult.messageBox.success.title";
    private static final String NUVEI_ACTION_RESULT_MESSAGE_BOX_ERROR_MESSAGE = "nuvei.actionResult.messageBox.failed.message";
    private static final String NUVEI_ACTION_RESULT_MESSAGE_BOX_SUCCESS_MESSAGE = "nuvei.actionResult.messageBox.success.message";

    private static final Logger LOG = LogManager.getLogger(NuveiManualCapturePaymentAction.class);

    @Resource(name = "businessProcessService")
    protected BusinessProcessService businessProcessService;
    @Resource(name = "processDefinitionDao")
    protected ProcessDefinitionDao processDefinitionDao;

    @Override
    public ActionResult<OrderModel> perform(final ActionContext<OrderModel> ctx) {
        if (ctx != null && ctx.getData() != null) {
            final OrderModel order = ctx.getData();

            getModelService().refresh(order);

            final List<BusinessProcessModel> waitingOrderProcesses =
                    processDefinitionDao.findWaitingOrderProcesses(order.getCode(), PaymentTransactionType.CAPTURE.getCode());

            if (CollectionUtils.isEmpty(waitingOrderProcesses)
                    || waitingOrderProcesses.size() > 1
                    || !OrderStatus.CAPTURE_PENDING.equals(order.getStatus())) {
                showErrorMessage(ctx);
                return new ActionResult<>(ActionResult.ERROR);
            }

            final BusinessProcessEvent.Builder builder = BusinessProcessEvent
                    .builder(waitingOrderProcesses.get(0).getCode() + SEPARATOR + PaymentTransactionType.CAPTURE.getCode())
                    .withChoice(BACKOFFICE);

            order.setStatus(OrderStatus.CAPTURE_STARTED);
            getModelService().save(order);
            LOG.info("Order with id [{}] status updated to [{}]", order.getCode(), OrderStatus.CAPTURE_STARTED.getCode());

            final BusinessProcessEvent event = builder.build();
            LOG.info("Triggering event {}", event);

            final boolean eventResult = businessProcessService.triggerEvent(event);

            if (eventResult) {
                showMessage(ctx);
                return new ActionResult<>(ActionResult.SUCCESS);
            } else {
                showErrorMessage(ctx);
                return new ActionResult<>(ActionResult.ERROR);
            }
        }

        return new ActionResult<>(ActionResult.ERROR);
    }

    /**
     * Displays a success information message
     *
     * @param actionContext The context of the action
     */
    protected void showMessage(final ActionContext<OrderModel> actionContext) {
        Messagebox.show(actionContext.getLabel(NUVEI_ACTION_RESULT_MESSAGE_BOX_SUCCESS_MESSAGE),
                actionContext.getLabel(NUVEI_ACTION_RESULT_MESSAGE_BOX_SUCCESS_TITLE), Messagebox.OK, Messagebox.INFORMATION);
    }

    /**
     * Displays an error information message
     *
     * @param actionContext The context of the action
     */
    protected void showErrorMessage(final ActionContext<OrderModel> actionContext) {
        Messagebox.show(actionContext.getLabel(NUVEI_ACTION_RESULT_MESSAGE_BOX_ERROR_MESSAGE),
                actionContext.getLabel(NUVEI_ACTION_RESULT_MESSAGE_BOX_ERROR_TITLE), Messagebox.OK, Messagebox.ERROR);
    }

    @Override
    public boolean canPerform(final ActionContext<OrderModel> ctx) {
        if (ctx != null && ctx.getData() != null) {

            final OrderModel order = ctx.getData();

            if (!OrderStatus.CAPTURE_PENDING.equals(order.getStatus()) || CollectionUtils.isEmpty(order.getOrderProcess()) ||
                    !CollectionUtils.isEmpty(order.getPaymentTransactions()) && order.getPaymentTransactions().size() > 1) {
                return false;
            }

            return Stream.ofNullable(order.getPaymentTransactions())
                    .flatMap(Collection::stream)
                    .map(PaymentTransactionModel::getEntries)
                    .flatMap(Stream::ofNullable)
                    .flatMap(List::stream)
                    .anyMatch(this::checkAuthorizationSuccess);
        }

        return false;
    }

    /**
     * Returns true if the Status of {@link PaymentTransactionEntryModel} is {@code APPROVED} or {@code SUCCESS}
     * and Type of {@link PaymentTransactionEntryModel} is @code AUTHORIZATION}
     *
     * @param paymentTransactionEntryModel the {@link PaymentTransactionEntryModel}
     * @return Returns true if the Status of {@link PaymentTransactionEntryModel} is {@code APPROVED} or {@code SUCCESS}
     * and Type of {@link PaymentTransactionEntryModel} is @code AUTHORIZATION}
     */
    protected boolean checkAuthorizationSuccess(final PaymentTransactionEntryModel paymentTransactionEntryModel) {
        return PaymentTransactionType.AUTHORIZATION.equals(paymentTransactionEntryModel.getType()) && (
                NuveiTransactionStatus.APPROVED.getCode().equals(paymentTransactionEntryModel.getTransactionStatus())
                        || NuveiTransactionStatus.SUCCESS.getCode().equals(paymentTransactionEntryModel.getTransactionStatus()));
    }

}
