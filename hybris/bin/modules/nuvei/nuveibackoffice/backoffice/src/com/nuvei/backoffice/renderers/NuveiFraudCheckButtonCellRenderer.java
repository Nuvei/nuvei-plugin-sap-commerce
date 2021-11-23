package com.nuvei.backoffice.renderers;

import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.omsbackoffice.renderers.FraudCheckButtonCellRenderer;
import de.hybris.platform.processengine.BusinessProcessEvent;
import de.hybris.platform.processengine.model.BusinessProcessModel;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Custom implementation of {@link FraudCheckButtonCellRenderer} to trigger our custom event
 */
public class NuveiFraudCheckButtonCellRenderer extends FraudCheckButtonCellRenderer {

    private static final String SEPARATOR = "_";
    private static final String EVENT = "CAPTURE";
    private static final String CSA_ORDER_VERIFIED = "CSAOrderVerified";

    @Override
    protected void executeFraudulentOperation(final OrderModel order) {
        getModelService().save(order);
        Stream.ofNullable(order.getOrderProcess())
                .flatMap(Collection::stream)
                .map(BusinessProcessModel::getCode)
                .filter(processCode ->  processCode.startsWith(order.getStore().getSubmitOrderProcessCode()))
                .forEach(this::triggerEvent);
    }

    private boolean triggerEvent(String filteredProcessCode) {
        return getBusinessProcessService()
                .triggerEvent(BusinessProcessEvent.builder(filteredProcessCode + SEPARATOR + EVENT)
                        .withChoice(CSA_ORDER_VERIFIED)
                        .build());
    }
}
