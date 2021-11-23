package com.nuvei.fraud.strategies;

import com.nuvei.notifications.enums.NuveiTransactionStatus;
import com.nuvei.services.enums.FinalFraudDecisionEnum;
import com.nuvei.services.enums.SystemDecisionEnum;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.fraud.impl.FraudServiceResponse;
import de.hybris.platform.payment.enums.PaymentTransactionType;
import de.hybris.platform.payment.model.PaymentTransactionEntryModel;
import de.hybris.platform.payment.model.PaymentTransactionModel;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class NuveiManualReviewStrategyTest {

	private static final Double INCREMENT = 20d;
	private static final String SYMPTON = "sympton";
	private static final String EXPLANATION = "explanation";

	@InjectMocks
	private NuveiManualReviewStrategy testObj;

	@Mock
	private FraudServiceResponse fraudServiceResponseMock;

	private PaymentTransactionEntryModel paymentTransactionEntryModelStub, paymentTransactionEntryModelTwoStub;
	private final PaymentTransactionModel paymentTransactionModelStub = new PaymentTransactionModel();
	private final AbstractOrderModel abstractOrderModelStub = new AbstractOrderModel();

	@Before
	public void setUp() {
		paymentTransactionEntryModelStub = new PaymentTransactionEntryModel();
		paymentTransactionEntryModelTwoStub = new PaymentTransactionEntryModel();
	}

	@Test
	public void execute() {
		testObj.setIncrement(INCREMENT);
		testObj.setSymptomName(SYMPTON);
		testObj.setExplanation(EXPLANATION);

		final FraudServiceResponse result = testObj.execute(Pair.of(abstractOrderModelStub, fraudServiceResponseMock));

		assertThat(result.getSymptoms()).isNotNull();
	}

	@Test
	public void isApplicable_shouldReturnTrue_whenOnePaymentTransactionEntryModelMatches() {
		paymentTransactionEntryModelStub.setType(PaymentTransactionType.AUTHORIZATION);
		paymentTransactionEntryModelStub.setSystemDecision(SystemDecisionEnum.REVIEW);
		paymentTransactionEntryModelStub.setFinalFraudDecision(FinalFraudDecisionEnum.ACCEPT);
		paymentTransactionEntryModelTwoStub.setType(PaymentTransactionType.AUTHORIZATION);
		paymentTransactionEntryModelTwoStub.setTransactionStatus(NuveiTransactionStatus.PENDING.getCode());
		paymentTransactionModelStub.setEntries(List.of(paymentTransactionEntryModelStub,paymentTransactionEntryModelTwoStub));
		abstractOrderModelStub.setPaymentTransactions(Collections.singletonList(paymentTransactionModelStub));

		final boolean result = testObj.isApplicable(Pair.of(abstractOrderModelStub, fraudServiceResponseMock));

		assertThat(result).isTrue();
	}

	@Test
	public void isApplicable_shouldReturnFalse_whenPaymentTransactionEntryModelNotMatches() {
		paymentTransactionEntryModelStub.setType(PaymentTransactionType.AUTHORIZATION);
		paymentTransactionEntryModelStub.setSystemDecision(SystemDecisionEnum.ACCEPT);
		paymentTransactionEntryModelStub.setFinalFraudDecision(FinalFraudDecisionEnum.ACCEPT);
		paymentTransactionEntryModelTwoStub.setType(PaymentTransactionType.AUTHORIZATION);
		paymentTransactionEntryModelTwoStub.setSystemDecision(SystemDecisionEnum.NONE);
		paymentTransactionEntryModelTwoStub.setFinalFraudDecision(FinalFraudDecisionEnum.ACCEPT);
		paymentTransactionModelStub.setEntries(List.of(paymentTransactionEntryModelStub,paymentTransactionEntryModelTwoStub));
		abstractOrderModelStub.setPaymentTransactions(Collections.singletonList(paymentTransactionModelStub));

		final boolean result = testObj.isApplicable(Pair.of(abstractOrderModelStub, fraudServiceResponseMock));

		assertThat(result).isFalse();
	}

	@Test
	public void isApplicable_shouldReturnFalse_whenThereAreNotPaymentTransactionEntryModels() {
		abstractOrderModelStub.setPaymentTransactions(Collections.singletonList(paymentTransactionModelStub));

		final boolean result = testObj.isApplicable(Pair.of(abstractOrderModelStub, fraudServiceResponseMock));

		assertThat(result).isFalse();
	}

	@Test
	public void findManualReviewTransaction_shouldReturnPaymentTransactionEntry_whenTypeAndSystemDecisionMatches() {
		paymentTransactionEntryModelStub.setType(PaymentTransactionType.AUTHORIZATION);
		paymentTransactionEntryModelStub.setSystemDecision(SystemDecisionEnum.REVIEW);
		paymentTransactionEntryModelStub.setFinalFraudDecision(FinalFraudDecisionEnum.ACCEPT);
		paymentTransactionEntryModelTwoStub.setType(PaymentTransactionType.AUTHORIZATION);
		paymentTransactionEntryModelTwoStub.setTransactionStatus(NuveiTransactionStatus.PENDING.getCode());
		abstractOrderModelStub.setPaymentTransactions(Collections.singletonList(paymentTransactionModelStub));
		paymentTransactionModelStub.setEntries(List.of(paymentTransactionEntryModelStub,paymentTransactionEntryModelTwoStub));

		final PaymentTransactionEntryModel result = testObj.findFraudTransaction(abstractOrderModelStub);

		assertThat(result).isEqualTo(paymentTransactionEntryModelStub);
	}

	@Test
	public void findManualReviewTransaction_shouldReturnNull_whenTypeAndSystemDecisionMatches() {
		paymentTransactionEntryModelStub.setType(PaymentTransactionType.AUTHORIZATION);
		paymentTransactionEntryModelStub.setSystemDecision(SystemDecisionEnum.ACCEPT);
		paymentTransactionEntryModelStub.setFinalFraudDecision(FinalFraudDecisionEnum.ACCEPT);
		paymentTransactionEntryModelTwoStub.setType(PaymentTransactionType.AUTHORIZATION);
		paymentTransactionEntryModelTwoStub.setSystemDecision(SystemDecisionEnum.NONE);
		paymentTransactionEntryModelTwoStub.setFinalFraudDecision(FinalFraudDecisionEnum.ACCEPT);
		abstractOrderModelStub.setPaymentTransactions(Collections.singletonList(paymentTransactionModelStub));
		paymentTransactionModelStub.setEntries(List.of(paymentTransactionEntryModelStub,paymentTransactionEntryModelTwoStub));

		final PaymentTransactionEntryModel result = testObj.findFraudTransaction(abstractOrderModelStub);

		assertThat(result).isNull();
	}

	@Test
	public void findManualReviewTransaction_shouldReturnNull_whenThereAreNotPaymentTransactionEntryModels() {
		abstractOrderModelStub.setPaymentTransactions(Collections.singletonList(paymentTransactionModelStub));
		paymentTransactionModelStub.setEntries(List.of(paymentTransactionEntryModelStub,paymentTransactionEntryModelTwoStub));

		final PaymentTransactionEntryModel result = testObj.findFraudTransaction(abstractOrderModelStub);

		assertThat(result).isNull();
	}

	@Test
	public void isManualReviewTransaction_shouldReturnTrue_whenTypeIsAuthorizationAndSystemDecisionIsReviewAndFraudDistinctReject() {
		paymentTransactionEntryModelStub.setType(PaymentTransactionType.AUTHORIZATION);
		paymentTransactionEntryModelStub.setSystemDecision(SystemDecisionEnum.REVIEW);
		paymentTransactionEntryModelStub.setFinalFraudDecision(FinalFraudDecisionEnum.ACCEPT);

		final boolean result = testObj.isManualReviewTransaction(paymentTransactionEntryModelStub);

		assertThat(result).isTrue();
	}

	@Test
	public void isManualReviewTransaction_shouldReturnFalse_whenTypeIsAuthorizationAndSystemDecisionIsReviewAndFraudIsReject() {
		paymentTransactionEntryModelStub.setType(PaymentTransactionType.AUTHORIZATION);
		paymentTransactionEntryModelStub.setSystemDecision(SystemDecisionEnum.REVIEW);
		paymentTransactionEntryModelStub.setFinalFraudDecision(FinalFraudDecisionEnum.REJECT);

		final boolean result = testObj.isManualReviewTransaction(paymentTransactionEntryModelStub);

		assertThat(result).isFalse();
	}
}
