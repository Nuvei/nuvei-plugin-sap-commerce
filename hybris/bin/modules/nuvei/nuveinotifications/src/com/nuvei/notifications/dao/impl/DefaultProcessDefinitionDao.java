package com.nuvei.notifications.dao.impl;

import com.nuvei.notifications.dao.ProcessDefinitionDao;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.orderprocessing.model.OrderProcessModel;
import de.hybris.platform.processengine.model.BusinessProcessModel;
import de.hybris.platform.processengine.model.ProcessTaskModel;
import de.hybris.platform.servicelayer.internal.dao.AbstractItemDao;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.SearchResult;

import java.util.List;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;

/**
 * The default implementation of {@link ProcessDefinitionDao} interface.
 */
public class DefaultProcessDefinitionDao extends AbstractItemDao implements ProcessDefinitionDao {

    protected static final String WAIT_ID_PREFIX = "waitFor_";
    protected static final String QUERY_PARAM_ORDER_CODE = "orderCode";
    protected static final String QUERY_PARAM_ACTION_TYPE = "actionType";

    protected static final String GET_BUSINESS_PROCESS_QUERY = "" +
            "SELECT x.PK FROM ({{" +
            " SELECT {op.PK} FROM {" + OrderProcessModel._TYPECODE + " AS op" +
            " JOIN " + ProcessTaskModel._TYPECODE + " AS pt ON {op.pk} = {pt." + ProcessTaskModel.PROCESS + "}" +
            " JOIN " + OrderModel._TYPECODE + " AS o ON {op." + OrderProcessModel.ORDER + "} = {o.PK}}" +
            " WHERE {pt." + ProcessTaskModel.ACTION + "} = ?" + QUERY_PARAM_ACTION_TYPE +
            " AND {o." + OrderModel.CODE + "} = ?" + QUERY_PARAM_ORDER_CODE +
            " }}) x";

    /**
     * {@inheritDoc}
     *
     * @see ProcessDefinitionDao#findWaitingOrderProcesses(String, String)
     */
    @Override
    public List<BusinessProcessModel> findWaitingOrderProcesses(final String orderCode, final String paymentTransactionType) {
        validateParameterNotNull(paymentTransactionType, "Transaction type must not be null");
        validateParameterNotNull(orderCode, "Order code must not be null");

        final FlexibleSearchQuery query = new FlexibleSearchQuery(GET_BUSINESS_PROCESS_QUERY);
        query.addQueryParameter(QUERY_PARAM_ACTION_TYPE, WAIT_ID_PREFIX + paymentTransactionType);
        query.addQueryParameter(QUERY_PARAM_ORDER_CODE, orderCode);
        final SearchResult<BusinessProcessModel> searchResult = getFlexibleSearchService().search(query);

        return searchResult.getResult();
    }
}
