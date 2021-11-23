package com.nuvei.fraud.impl;

import com.nuvei.strategy.NuveiAbstractStrategyExecutor;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.fraud.impl.FraudServiceResponse;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;


@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class NuveiFraudSymptomTest {

    @InjectMocks
    private NuveiFraudSymptom testObj;

    @Mock
    private NuveiAbstractStrategyExecutor<Pair<AbstractOrderModel, FraudServiceResponse>, FraudServiceResponse> fraudSymptomStrategyExecutorMock;

    @Mock
    private FraudServiceResponse fraudServiceResponseMock, fraudServiceResponseTwoMock;
    @Mock
    private AbstractOrderModel abstractOrderModelMockModel;

    @Test
    public void recognizeSymptom_shouldReturnFraudServiceResponseResult_whenResultIsNotNull() {

        when(fraudSymptomStrategyExecutorMock.execute(Pair.of(abstractOrderModelMockModel, fraudServiceResponseMock))).thenReturn(fraudServiceResponseTwoMock);

        final FraudServiceResponse result = testObj.recognizeSymptom(fraudServiceResponseMock, abstractOrderModelMockModel);

        assertThat(result).isEqualTo(fraudServiceResponseTwoMock);
    }

    @Test
    public void recognizeSymptom_shouldReturnFraudServiceResponseOriginal_whenResultIsNull() {

        when(fraudSymptomStrategyExecutorMock.execute(Pair.of(abstractOrderModelMockModel, null))).thenReturn(null);

        final FraudServiceResponse result = testObj.recognizeSymptom(fraudServiceResponseMock, abstractOrderModelMockModel);

        assertThat(result).isEqualTo(fraudServiceResponseMock);
    }

}
