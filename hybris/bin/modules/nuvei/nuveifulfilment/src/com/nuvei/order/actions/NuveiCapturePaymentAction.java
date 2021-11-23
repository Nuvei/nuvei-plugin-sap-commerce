package com.nuvei.order.actions;

import com.nuvei.services.exchange.NuveiExchangeService;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.orderprocessing.model.OrderProcessModel;
import de.hybris.platform.processengine.action.AbstractAction;
import de.hybris.platform.processengine.action.AbstractProceduralAction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.Set;

public class NuveiCapturePaymentAction extends AbstractProceduralAction<OrderProcessModel> {
    private static final Logger LOG = LogManager.getLogger(NuveiCapturePaymentAction.class);

    protected final NuveiExchangeService nuveiSettleService;

    protected static final String OK = "OK";

    /**
     * Default constructor for {@link NuveiCapturePaymentAction}
     *
     * @param nuveiSettleService
     */
    public NuveiCapturePaymentAction(final NuveiExchangeService nuveiSettleService) {
        this.nuveiSettleService = nuveiSettleService;
    }

    /**
     * Execute action from business process that make settle request to capture payment
     *
     * @param businessProcessModel
     * @return
     */
    @Override
    public void executeAction(final OrderProcessModel businessProcessModel) {
        final OrderModel order = businessProcessModel.getOrder();

        if (Objects.nonNull(order)) {
            LOG.info("Trying to capture payment for order {}", order.getCode());
            try {
                nuveiSettleService.requestSafechargeTransaction(order);
            } catch (final Exception e) {
                LOG.error(String.format("Error to capture payment for order [%s]", order.getCode()), e);
                setOrderStatus(order, OrderStatus.CAPTURE_PENDING);
                LOG.info("Order with id [{}] status updated to [{}]", order.getCode(), OrderStatus.CAPTURE_PENDING.getCode());
                LOG.error("Not possible capture payment for process [{}]", businessProcessModel.getCode());
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> getTransitions() {
        return AbstractAction.createTransitions(OK);
    }
}
