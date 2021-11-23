package com.nuvei.strategy;

import de.hybris.bootstrap.annotations.UnitTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;


@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class NuveiAbstractStrategyExecutorTest {

    private static final String SOURCE = "source";
    private static final String RESULT_FIRST_STRATEGY = "resultFirstStrategy";
    private static final String RESULT_SECOND_STRATEGY = "resultSecondStrategy";

    @Spy
    @InjectMocks
    private MyNuveiAbstractStrategyExecutor testObj;
    @Mock
    private NuveiStrategy<String, String> firstStrategy, secondStrategy;


    @Before
    public void setUp() {
        when(testObj.getStrategies()).thenReturn(List.of(firstStrategy, secondStrategy));
        when(firstStrategy.execute(SOURCE)).thenReturn(RESULT_FIRST_STRATEGY);
        when(secondStrategy.execute(SOURCE)).thenReturn(RESULT_SECOND_STRATEGY);
    }

    @Test
    public void execute_ShouldReturnNull_WhenThereIsNoApplicableStrategy() {
        when(firstStrategy.isApplicable(SOURCE)).thenReturn(false);
        when(secondStrategy.isApplicable(SOURCE)).thenReturn(false);

        final String result = testObj.execute(SOURCE);
        verify(firstStrategy,never()).execute(SOURCE);
        verify(secondStrategy,never()).execute(SOURCE);

        assertThat(result).isNull();
    }

    @Test
    public void execute_ShouldExecuteTheApplicableStrategy_WhenHasApplicableStrategy() {
        when(firstStrategy.isApplicable(SOURCE)).thenReturn(true);
        when(secondStrategy.isApplicable(SOURCE)).thenReturn(false);

        final String result = testObj.execute(SOURCE);
        verify(firstStrategy).execute(SOURCE);
        verify(secondStrategy,never()).execute(SOURCE);

        assertThat(result).isEqualTo(RESULT_FIRST_STRATEGY);
    }

    @Test
    public void execute_ShouldExecuteTheFirstApplicableStrategy_WhenHasMoreThanOneApplicableStrategy() {
        when(firstStrategy.isApplicable(SOURCE)).thenReturn(true);
        when(secondStrategy.isApplicable(SOURCE)).thenReturn(true);

        final String result = testObj.execute(SOURCE);
        verify(firstStrategy).execute(SOURCE);
        verify(secondStrategy,never()).execute(SOURCE);

        assertThat(result).isEqualTo(RESULT_FIRST_STRATEGY);
    }

    public static class MyNuveiAbstractStrategyExecutor extends NuveiAbstractStrategyExecutor<String, String> {

    }
}
