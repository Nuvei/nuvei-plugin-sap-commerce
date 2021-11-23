package com.nuvei.notifications.populators;

import com.nuvei.notifications.data.NuveiIncomingDMNData;
import com.nuvei.notifications.enums.NuveiPPPTransactionStatus;
import com.nuvei.notifications.enums.NuveiTransactionStatus;
import com.nuvei.notifications.model.NuveiDirectMerchantNotificationModel;
import com.nuvei.services.enums.FinalFraudDecisionEnum;
import com.nuvei.services.enums.NuveiTransactionType;
import com.nuvei.services.enums.SystemDecisionEnum;
import com.nuvei.services.service.NuveiPaymentMethodService;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.enumeration.EnumerationService;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;

import java.math.BigDecimal;
import java.util.Optional;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;

/**
 * Populate from {@link NuveiIncomingDMNData} to {@link NuveiDirectMerchantNotificationModel}
 */
public class NuveiDMNPopulator implements Populator<NuveiIncomingDMNData, NuveiDirectMerchantNotificationModel> {

    private static final String SOURCE_MUST_NOT_BE_NULL = "Parameter source must not be null";
    private static final String TARGET_MUST_NOT_BE_NULL = "Parameter target must not be null";

    protected final EnumerationService enumerationService;
    protected final NuveiPaymentMethodService nuveiPaymentMethodService;
    protected final CommonI18NService commonI18NService;

    public NuveiDMNPopulator(final EnumerationService enumerationService,
                             final NuveiPaymentMethodService nuveiPaymentMethodService,
                             final CommonI18NService commonI18NService) {
        this.enumerationService = enumerationService;
        this.nuveiPaymentMethodService = nuveiPaymentMethodService;
        this.commonI18NService = commonI18NService;
    }

    /**
     * Populate from {@link NuveiIncomingDMNData} to {@link NuveiDirectMerchantNotificationModel}
     *
     * @param source {@link NuveiIncomingDMNData}
     * @param target {@link NuveiDirectMerchantNotificationModel}
     * @throws ConversionException
     */
    @Override
    public void populate(final NuveiIncomingDMNData source, final NuveiDirectMerchantNotificationModel target) throws ConversionException {
        validateParameterNotNull(source, SOURCE_MUST_NOT_BE_NULL);
        validateParameterNotNull(target, TARGET_MUST_NOT_BE_NULL);

        target.setRawNotification(source);
        Optional.ofNullable(source.getMerchant_id()).ifPresent(target::setMerchantId);
        Optional.ofNullable(source.getMerchant_site_id()).ifPresent(target::setMerchantSiteId);
        Optional.ofNullable(source.getMerchant_unique_id()).ifPresent(target::setMerchantUniqueId);
        Optional.ofNullable(source.getClientUniqueId()).ifPresent(target::setClientUniqueId);
        Optional.ofNullable(source.getPPP_TransactionID()).ifPresent(target::setPppTransactionId);
        Optional.ofNullable(source.getTransactionID()).ifPresent(target::setTransactionId);
        Optional.ofNullable(source.getRelatedTransactionId()).ifPresent(target::setRelatedTransactionId);
        Optional.ofNullable(source.getClientRequestId()).ifPresent(target::setClientRequestId);
        Optional.ofNullable(source.getPayment_method())
                .map(nuveiPaymentMethodService::findNuveiPaymentMethodById)
                .ifPresent(target::setPaymentMethod);
        Optional.ofNullable(source.getTransactionType())
                .map(s -> enumerationService.getEnumerationValue(NuveiTransactionType.class, s))
                .ifPresent(target::setTransactionType);
        Optional.ofNullable(source.getStatus())
                .map(s -> enumerationService.getEnumerationValue(NuveiTransactionStatus.class, s))
                .ifPresent(target::setStatus);
        Optional.ofNullable(source.getPpp_status())
                .map(s -> enumerationService.getEnumerationValue(NuveiPPPTransactionStatus.class, s))
                .ifPresent(target::setPppStatus);
        Optional.ofNullable(source.getCurrency())
                .map(commonI18NService::getCurrency)
                .ifPresent(target::setCurrency);
        Optional.ofNullable(source.getTotalAmount())
                .map(BigDecimal::new)
                .ifPresent(target::setTotalAmount);
        Optional.ofNullable(source.getSystemDecision())
                .map(s -> enumerationService.getEnumerationValue(SystemDecisionEnum.class, s))
                .ifPresent(target::setSystemDecision);
        Optional.ofNullable(source.getFinalFraudDecision())
                .map(s -> enumerationService.getEnumerationValue(FinalFraudDecisionEnum.class, s))
                .ifPresent(target::setFinalFraudDecision);
    }
}
