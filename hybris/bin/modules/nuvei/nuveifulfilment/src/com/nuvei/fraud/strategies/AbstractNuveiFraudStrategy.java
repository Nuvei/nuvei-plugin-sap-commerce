package com.nuvei.fraud.strategies;

import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.fraud.impl.FraudServiceResponse;
import de.hybris.platform.fraud.impl.FraudSymptom;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.Logger;


public abstract class AbstractNuveiFraudStrategy {
    private final Logger log;
    private String symptomName;
    private String explanation;
    private double increment;

    protected AbstractNuveiFraudStrategy(final Logger log) {
        this.log = log;
    }

    public FraudServiceResponse execute(final Pair<AbstractOrderModel, FraudServiceResponse> source) {
        final FraudServiceResponse fraudServiceResponse = source.getRight();
        fraudServiceResponse.addSymptom(new FraudSymptom(getExplanation(), getIncrement(), getSymptomName()));
        log.info("Order with Id [{}] got a score of {} evaluating the symptom {}, reason: {}", source.getLeft().getCode(), getIncrement(), getSymptomName(), getExplanation());
        return fraudServiceResponse;
    }


    public String getSymptomName() {
        return symptomName;
    }

    public void setSymptomName(final String symptomName) {
        this.symptomName = symptomName;
    }

    public double getIncrement() {
        return increment;
    }

    public void setIncrement(final double increment) {
        this.increment = increment;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(final String explanation) {
        this.explanation = explanation;
    }
}
