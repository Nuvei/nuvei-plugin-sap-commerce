package com.nuvei.services.handlers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.model.attribute.DynamicAttributeHandler;
import org.apache.commons.lang.StringUtils;

import java.io.Serializable;

/**
 * Parametrized dynamic attribute handler to stringify an serializable object attribute into json using Gson
 */
public class SerializableToJsonAttributeHandler implements DynamicAttributeHandler<String, ItemModel> {
    protected final Gson gson = new GsonBuilder().setPrettyPrinting()
            .disableHtmlEscaping()
            .create();
    protected final String attributeName;
    protected final ModelService modelService;

    public SerializableToJsonAttributeHandler(final String attributeName, final ModelService modelService) {
        this.attributeName = attributeName;
        this.modelService = modelService;
    }

    @Override
    public String get(final ItemModel model) {
        final Object attributeValue = modelService.getAttributeValue(model, attributeName);
        if (attributeValue == null) {
            return StringUtils.EMPTY;
        } else if (attributeValue instanceof Serializable) {
            return stringify(attributeValue);
        } else {
            throw new UnsupportedOperationException(String.format("The attribute [%s] for item with PK [%s] does not implements serializable",
                    attributeValue, model.getPk()));
        }
    }

    @Override
    public void set(final ItemModel model, final String s) {
        throw new UnsupportedOperationException("Write is not a valid operation for this dynamic attribute");
    }

    /**
     * Convert json object into a String
     *
     * @param attributeValue to convert
     * @return String with json converted
     */
    protected String stringify(final Object attributeValue) {
        return gson.toJson(attributeValue);
    }
}
