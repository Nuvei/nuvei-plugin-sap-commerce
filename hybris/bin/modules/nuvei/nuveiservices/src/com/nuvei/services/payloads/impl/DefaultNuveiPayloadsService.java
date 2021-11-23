package com.nuvei.services.payloads.impl;

import com.nuvei.services.model.NuveiPayloadModel;
import com.nuvei.services.payloads.NuveiPayloadsService;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.servicelayer.model.ModelService;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;

public class DefaultNuveiPayloadsService implements NuveiPayloadsService {

    private final ModelService modelService;

    /**
     * Default constructor for {@link DefaultNuveiPayloadsService}
     *
     * @param modelService injected
     */
    public DefaultNuveiPayloadsService(final ModelService modelService) {
        this.modelService = modelService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPaymentRequestPayload(final String request, final AbstractOrderModel abstractOrder) {
        validateParameterNotNullStandardMessage("request", request);
        validateParameterNotNullStandardMessage("abstractOrder", abstractOrder);

        final NuveiPayloadModel payloadModel = createPayloadModel(request);
        payloadModel.setRequestOrder(abstractOrder);
        modelService.save(payloadModel);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPaymentResponsePayload(final String response, final AbstractOrderModel abstractOrder) {
        validateParameterNotNullStandardMessage("response", response);
        validateParameterNotNullStandardMessage("abstractOrder", abstractOrder);

        final NuveiPayloadModel payloadModel = createPayloadModel(response);
        payloadModel.setResponseOrder(abstractOrder);
        modelService.save(payloadModel);
    }

    /**
     * Creates a payload object with the given payload content
     *
     * @param payload The payload content
     * @return The payload object with the given content
     */
    protected NuveiPayloadModel createPayloadModel(final String payload) {
        final NuveiPayloadModel payloadModel = modelService.create(NuveiPayloadModel.class);
        payloadModel.setPayload(payload);
        return payloadModel;
    }

}
