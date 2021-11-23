package com.nuvei.strategy;

import org.apache.commons.collections.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Abstract strategy factory that ensures that only one strategy is run for an specific
 * source and target
 */
public class NuveiAbstractStrategyExecutor<S, T> implements NuveiStrategyExecutor<S, T> {

    private static final Logger LOG = LogManager.getLogger(NuveiAbstractStrategyExecutor.class);

    private List<NuveiStrategy<S, T>> strategies;

    /**
     * {@inheritDoc}
     */
    @Override
    public T execute(final S source) {
        if (CollectionUtils.isNotEmpty(getStrategies())) {
            final Optional<NuveiStrategy<S, T>> strategy = getStrategies()
                    .stream()
                    .filter(stNuveiStrategy -> stNuveiStrategy.isApplicable(source))
                    .findFirst();
            return strategy.map(stNuveiStrategy -> stNuveiStrategy.execute(source)).orElse(null);
        }

        LOG.warn("None of strategies {} has been executed", getStrategies().stream().map(stNuveiStrategy ->
                stNuveiStrategy.getClass().getName()).collect(Collectors.joining(",")));

        return null;
    }

    public List<NuveiStrategy<S, T>> getStrategies() {
        return strategies;
    }

    public void setStrategies(final List<NuveiStrategy<S, T>> strategies) {
        this.strategies = strategies;
    }
}
