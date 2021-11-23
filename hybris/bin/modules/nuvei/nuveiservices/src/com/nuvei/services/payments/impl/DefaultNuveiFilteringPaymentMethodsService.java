package com.nuvei.services.payments.impl;

import com.nuvei.services.model.NuveiMerchantConfigurationModel;
import com.nuvei.services.model.NuveiPaymentMethodModel;
import com.nuvei.services.payments.NuveiFilteringPaymentMethodsService;
import com.nuvei.services.wrapper.NuveiSafechargeWrapper;
import com.safecharge.exception.SafechargeException;
import com.safecharge.model.PaymentMethod;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.model.ModelService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class DefaultNuveiFilteringPaymentMethodsService implements NuveiFilteringPaymentMethodsService {

    private static final Logger LOG = LogManager.getLogger(DefaultNuveiFilteringPaymentMethodsService.class);

    protected final Converter<PaymentMethod, NuveiPaymentMethodModel> nuveiPaymentMethodsConverter;
    protected final ModelService modelService;

    /**
     * Default constructor for {@link DefaultNuveiFilteringPaymentMethodsService}
     *
     * @param nuveiPaymentMethodsConverter injected
     * @param modelService                 injected
     */
    public DefaultNuveiFilteringPaymentMethodsService(final Converter<PaymentMethod, NuveiPaymentMethodModel> nuveiPaymentMethodsConverter,
                                                      final ModelService modelService) {
        this.nuveiPaymentMethodsConverter = nuveiPaymentMethodsConverter;
        this.modelService = modelService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void synchFilteringPaymentMethodsForMerchant(final NuveiMerchantConfigurationModel merchantConfig) throws SafechargeException {
        final List<PaymentMethod> paymentMethods = getFilteringMerchantPaymentMethodsResponse(merchantConfig);

        synchFilteringMerchantPaymentMethods(merchantConfig, paymentMethods);
    }

    /**
     * Calls Nuvei Api to retrieve the MerchantPaymentMethods for the given merchant
     *
     * @param merchantConfig The merchant configuration for which to retrieve the payment methods
     * @return The GetMerchantPaymentMethodsResponse object that contains all payment methods
     * @throws SafechargeException if there are request related problems.
     */
    protected List<PaymentMethod> getFilteringMerchantPaymentMethodsResponse(final NuveiMerchantConfigurationModel merchantConfig) throws SafechargeException {
        final NuveiSafechargeWrapper nuveiSafechargeWrapper = new NuveiSafechargeWrapper(merchantConfig);
        return nuveiSafechargeWrapper.getMerchantPaymentMethods();
    }

    /**
     * Synchronizes the given {@link NuveiMerchantConfigurationModel} associated {@link NuveiPaymentMethodModel}
     *
     * @param merchantConfig The given {@link NuveiMerchantConfigurationModel} to synchronize
     * @param paymentMethods The list of {@link PaymentMethod}
     */
    protected void synchFilteringMerchantPaymentMethods(final NuveiMerchantConfigurationModel merchantConfig,
                                                        final List<PaymentMethod> paymentMethods) {
        removeNonExistentFilteringPaymentMethods(paymentMethods, merchantConfig);

        final Set<NuveiPaymentMethodModel> merchantPaymentMethods = merchantConfig.getPaymentMethods();

        final Set<PaymentMethod> newPaymentMethods = findNewFilteringPaymentMethods(paymentMethods, merchantPaymentMethods);

        if (CollectionUtils.isNotEmpty(newPaymentMethods)) {
            Set<NuveiPaymentMethodModel> modifiableMerchantPaymentMethods = new HashSet<>(merchantPaymentMethods);
            modifiableMerchantPaymentMethods.addAll(nuveiPaymentMethodsConverter.convertAll(newPaymentMethods));
            merchantConfig.setPaymentMethods(modifiableMerchantPaymentMethods);
            modelService.save(merchantConfig);

            LOG.info("Creating merchant {} payment methods: {}", merchantConfig.getCode(), newPaymentMethods.stream()
                    .map(PaymentMethod::getPaymentMethod)
                    .collect(Collectors.toList()));
        }
    }

    /**
     * Obtains the {@link PaymentMethod} that currently are not associated to the given
     * {@link NuveiMerchantConfigurationModel}
     *
     * @param paymentMethods         The list of {@link PaymentMethod} to check
     * @param merchantPaymentMethods The {@link NuveiMerchantConfigurationModel}
     * @return The {@link PaymentMethod} not associated to the {@link NuveiMerchantConfigurationModel}
     */
    protected Set<PaymentMethod> findNewFilteringPaymentMethods(final List<PaymentMethod> paymentMethods,
                                                                final Set<NuveiPaymentMethodModel> merchantPaymentMethods) {

        final List<String> merchantPaymentCodes = merchantPaymentMethods
                .stream()
                .map(NuveiPaymentMethodModel::getId)
                .collect(Collectors.toList());

        return paymentMethods.stream()
                .filter(paymentMethod -> !merchantPaymentCodes.contains(paymentMethod.getPaymentMethod()))
                .collect(Collectors.toSet());
    }

    /**
     * Removes the {@link NuveiPaymentMethodModel} that are no longer associated to the given
     * {@link NuveiMerchantConfigurationModel} in the Nuvei systems
     *
     * @param paymentMethods The list of {@link PaymentMethod} to check
     * @param merchantConfig The {@link NuveiMerchantConfigurationModel}
     */
    protected void removeNonExistentFilteringPaymentMethods(final List<PaymentMethod> paymentMethods,
                                                            final NuveiMerchantConfigurationModel merchantConfig) {
        final List<String> paymentCodes = paymentMethods
                .stream()
                .map(PaymentMethod::getPaymentMethod)
                .collect(Collectors.toList());

        final Set<NuveiPaymentMethodModel> paymentsToRemove = merchantConfig.getPaymentMethods().stream()
                .filter(merchantPayment -> !paymentCodes.contains(merchantPayment.getId()))
                .collect(Collectors.toSet());

        modelService.removeAll(paymentsToRemove);
        modelService.refresh(merchantConfig);

        LOG.info("Removed  merchant {} payment methods: {}", merchantConfig.getCode(), paymentsToRemove.stream()
                .map(NuveiPaymentMethodModel::getId)
                .collect(Collectors.toList()));
    }
}
