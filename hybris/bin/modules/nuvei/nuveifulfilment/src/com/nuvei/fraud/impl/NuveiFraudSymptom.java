package com.nuvei.fraud.impl;

import com.nuvei.strategy.NuveiAbstractStrategyExecutor;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.fraud.impl.FraudServiceResponse;
import de.hybris.platform.fraud.strategy.AbstractOrderFraudSymptomDetection;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Objects;

public class NuveiFraudSymptom extends AbstractOrderFraudSymptomDetection {

    private final NuveiAbstractStrategyExecutor<Pair<AbstractOrderModel, FraudServiceResponse>, FraudServiceResponse> fraudSymptomStrategyExecutor;

    public NuveiFraudSymptom(final NuveiAbstractStrategyExecutor<Pair<AbstractOrderModel, FraudServiceResponse>, FraudServiceResponse> fraudSymptomStrategyExecutor) {
        this.fraudSymptomStrategyExecutor = fraudSymptomStrategyExecutor;
    }

    @Override
    public FraudServiceResponse recognizeSymptom(final FraudServiceResponse fraudServiceResponse, final AbstractOrderModel abstractOrderModel) {
        final FraudServiceResponse response = fraudSymptomStrategyExecutor.execute(Pair.of(abstractOrderModel, fraudServiceResponse));
        return Objects.nonNull(response) ? response : fraudServiceResponse;
    }


}
