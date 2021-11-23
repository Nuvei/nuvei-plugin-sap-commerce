package com.nuvei.fraud.strategies;

import com.nuvei.services.enums.FinalFraudDecisionEnum;
import com.nuvei.services.enums.SystemDecisionEnum;
import com.nuvei.strategy.NuveiStrategy;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.fraud.impl.FraudServiceResponse;
import de.hybris.platform.fraud.impl.FraudSymptom;
import de.hybris.platform.payment.enums.PaymentTransactionType;
import de.hybris.platform.payment.model.PaymentTransactionEntryModel;
import de.hybris.platform.payment.model.PaymentTransactionModel;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class NuveiFraudStrategy extends AbstractNuveiFraudStrategy implements NuveiStrategy<Pair<AbstractOrderModel, FraudServiceResponse>, FraudServiceResponse> {

    private static final Logger log = LogManager.getLogger(NuveiFraudStrategy.class);

    public NuveiFraudStrategy() {
        super(log);
    }

    @Override
    public FraudServiceResponse execute(final Pair<AbstractOrderModel, FraudServiceResponse> source) {
        final FraudServiceResponse fraudServiceResponse = source.getRight();
        fraudServiceResponse.addSymptom(new FraudSymptom(getExplanation(), getIncrement(), getSymptomName()));
        log.info("Order with Id [{}] got a score of {} evaluating the symptom {}, reason: {}", source.getLeft().getCode(), getIncrement(), getSymptomName(), getExplanation());
        return fraudServiceResponse;
    }

    @Override
    public boolean isApplicable(final Pair<AbstractOrderModel, FraudServiceResponse> source) {
        final PaymentTransactionEntryModel paymentEntry = findManualReviewTransaction(source.getLeft());
        return Objects.nonNull(paymentEntry);
    }

    protected PaymentTransactionEntryModel findManualReviewTransaction(final AbstractOrderModel abstractOrderModel) {
        return Stream.ofNullable(abstractOrderModel.getPaymentTransactions())
                .flatMap(Collection::stream)
                .map(PaymentTransactionModel::getEntries)
                .flatMap(Stream::ofNullable)
                .flatMap(List::stream)
                .filter(this::isFraudTransaction)
                .findAny()
                .orElse(null);
    }

    protected boolean isFraudTransaction(final PaymentTransactionEntryModel paymentTransactionEntryModel) {
        return PaymentTransactionType.AUTHORIZATION.equals(paymentTransactionEntryModel.getType())
                && (FinalFraudDecisionEnum.REJECT.equals(paymentTransactionEntryModel.getFinalFraudDecision())
                || SystemDecisionEnum.REJECT.equals(paymentTransactionEntryModel.getSystemDecision())
                || SystemDecisionEnum.ERROR.equals(paymentTransactionEntryModel.getSystemDecision()));
    }
}
