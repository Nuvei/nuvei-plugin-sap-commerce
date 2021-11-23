package com.nuvei.services.exchange;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nuvei.services.model.NuveiMerchantConfigurationModel;
import com.nuvei.services.model.NuveiPayloadModel;
import com.nuvei.services.service.NuveiPaymentTransactionService;
import com.nuvei.services.wrapper.NuveiSafechargeWrapper;
import com.safecharge.request.SafechargeTransactionRequest;
import com.safecharge.response.SafechargeTransactionResponse;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.servicelayer.model.ModelService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;

public abstract class AbstractNuveiExchangeService {

    protected static final Logger LOG = LogManager.getLogger(AbstractNuveiExchangeService.class);

    protected static final String ERROR_STORING_REQUEST = "Error storing the settle request to the order [{}]";
    protected static final String ERROR_STORING_RESPONSE = "Error storing the settle response to the order [{}]";

    protected final ModelService modelService;
    protected final NuveiPaymentTransactionService nuveiPaymentTransactionService;

    /**
     * @param modelService
     * @param nuveiPaymentTransactionService
     */
    protected AbstractNuveiExchangeService(final ModelService modelService, final NuveiPaymentTransactionService nuveiPaymentTransactionService) {
        this.modelService = modelService;
        this.nuveiPaymentTransactionService = nuveiPaymentTransactionService;
    }

    /**
     * Method to store the safecharge response to the order as raw payload
     *
     * @param orderModel          order to store the response
     * @param transactionResponse safecharge response
     */
    public void storeSafechargeResponse(final OrderModel orderModel, final SafechargeTransactionResponse transactionResponse) {
        try {
            final NuveiPayloadModel responsePayload = modelService.create(NuveiPayloadModel.class);
            responsePayload.setPayload(new ObjectMapper().writeValueAsString(transactionResponse));
            final Collection<NuveiPayloadModel> responsesPayload = new ArrayList<>(orderModel.getResponsesPayload());
            responsesPayload.add(responsePayload);
            orderModel.setResponsesPayload(responsesPayload);
        } catch (final JsonProcessingException e) {
            LOG.error(ERROR_STORING_RESPONSE, orderModel.getCode());
        }
    }

    /**
     * Method to store the safecharge request to the order as raw payload
     *
     * @param orderModel         order to store the request
     * @param transactionRequest safecharge request
     */
    public void storeSafechargeRequest(final OrderModel orderModel, final SafechargeTransactionRequest transactionRequest) {
        try {
            final NuveiPayloadModel requestPayload = modelService.create(NuveiPayloadModel.class);
            requestPayload.setPayload(new ObjectMapper().writeValueAsString(transactionRequest));
            final Collection<NuveiPayloadModel> requestsPayload = new ArrayList<>(orderModel.getRequestsPayload());
            requestsPayload.add(requestPayload);
            orderModel.setRequestsPayload(requestsPayload);
        } catch (final JsonProcessingException e) {
            LOG.error(ERROR_STORING_REQUEST, orderModel.getCode());
        }
    }

    /**
     * Return new {@link NuveiSafechargeWrapper}
     *
     * @param merchantConfiguration
     * @return {@link NuveiSafechargeWrapper}
     */
    public NuveiSafechargeWrapper getNuveiSafechargeWrapper(final NuveiMerchantConfigurationModel merchantConfiguration) {
        return new NuveiSafechargeWrapper(merchantConfiguration);
    }
}
