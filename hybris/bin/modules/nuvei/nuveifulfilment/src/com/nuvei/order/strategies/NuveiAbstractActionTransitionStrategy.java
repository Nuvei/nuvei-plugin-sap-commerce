package com.nuvei.order.strategies;

import com.nuvei.strategy.NuveiStrategy;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.servicelayer.model.ModelService;
import org.apache.logging.log4j.Logger;

/**
 * Abstract class for injecting {@link ModelService} and a method for updating easily the status of the order with a
 * {@link OrderStatus}
 */
public abstract class NuveiAbstractActionTransitionStrategy implements NuveiStrategy<OrderModel, String> {
    private final Logger log;

    protected static final String OK = "OK";
    protected static final String NOK = "NOK";
    protected static final String WAIT = "WAIT";

    private final ModelService modelService;

    /**
     * Default constructor for {@link NuveiAbstractActionTransitionStrategy}
     *
     * @param log          injected
     * @param modelService injected
     */
    protected NuveiAbstractActionTransitionStrategy(final Logger log, final ModelService modelService) {
        this.log = log;
        this.modelService = modelService;
    }

    /**
     * Sets the order status of a {@link OrderModel} to the {@code status} passed as param
     *
     * @param order  The {@link OrderModel} to update
     * @param status {@link OrderStatus}
     */
    protected void updateOrderStatus(final OrderModel order, final OrderStatus status) {
        order.setStatus(status);
        modelService.save(order);
        log.info("Order with id [{}] status updated to [{}]", order.getCode(), status.getCode());
    }
}
