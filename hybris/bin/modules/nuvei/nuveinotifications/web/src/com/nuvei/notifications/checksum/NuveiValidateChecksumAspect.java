package com.nuvei.notifications.checksum;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nuvei.notifications.data.NuveiIncomingDMNData;
import com.nuvei.services.merchant.NuveiMerchantConfigurationService;
import com.nuvei.services.model.NuveiMerchantConfigurationModel;
import com.nuvei.services.util.NuveiHashAlgorithmUtil;
import com.safecharge.util.Constants;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.lang.annotation.Annotation;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * This class ensures the calculated checksum with the secret of the merchant is equal to the one received in the DMN
 */
public class NuveiValidateChecksumAspect {
    private static final Logger LOG = LogManager.getLogger(NuveiValidateChecksumAspect.class);

    protected final Gson gson = new GsonBuilder().setPrettyPrinting()
            .disableHtmlEscaping()
            .create();
    protected final NuveiMerchantConfigurationService nuveiMerchantConfigurationService;

    public NuveiValidateChecksumAspect(final NuveiMerchantConfigurationService nuveiMerchantConfigurationService) {
        this.nuveiMerchantConfigurationService = nuveiMerchantConfigurationService;
    }

    /**
     * This class catches the {@link ProceedingJoinPoint} and ensures the method annotated only executes it's logic
     * if the {@code advanceresponsechecksum} property of the {@link NuveiIncomingDMNData} is valid with the one generated
     * as described in
     * <a href="https://docs.safecharge.com/documentation/guides/dmns/#authenticating-a-dmn-checksum">Nuvei Documentation</a>
     *
     * @param pjp the {@link ProceedingJoinPoint}
     * @return the incoming {@link ProceedingJoinPoint} object if checksum is valid
     * @throws Throwable if checksum is invalid or the annotation is not linked to a {@link NuveiIncomingDMNData}
     */
    public Object validateChecksum(final ProceedingJoinPoint pjp) throws NoSuchMethodException {
        final MethodSignature signature = (MethodSignature) pjp.getSignature();
        final String methodName = signature.getMethod().getName();
        final Class<?>[] parameterTypes = signature.getMethod().getParameterTypes();
        final Annotation[][] annotations;
        try {
            annotations = pjp.getTarget().getClass().getMethod(methodName, parameterTypes).getParameterAnnotations();
        } catch (final NoSuchMethodException e) {
            throw new NoSuchMethodException(e.getMessage());
        }
        final Set<Integer> annotatedParams = getParameterIndexesForAnnotatedParam(parameterTypes, annotations);

        final boolean checksumFailed = annotatedParams.stream().noneMatch(paramIndex -> internalValidateChecksum(paramIndex, pjp.getArgs()));

        if (checksumFailed || annotatedParams.isEmpty()) {
            LOG.error("The validation or the processing of the checksum has failed");
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "The validation or the processing of the checksum has failed");
        }

        try {
            return pjp.proceed();
        } catch (final Throwable e) {
            throw new NoSuchMethodException(e.getMessage());
        }
    }

    /**
     * This method retrieves the actual object in the {@code position} for the {@code args} and validates the
     * {@code advanceresponsechecksum} agains the checksum generated through the concatenation of the
     * {@code merchantSecretKey + totalAmount + currency + responseTimeStamp + pppTransactionId + status} with a
     * {@code SHA256}
     *
     * @param position The position where the object sits within the {@code args} argument
     * @param args     The {@link Object} array that contains all the params in the method
     * @return {@code true} when the checksum is valid and {@code false} if does not
     */
    protected boolean internalValidateChecksum(final Integer position, final Object[] args) {
        final NuveiIncomingDMNData dmnNotificationData = getDMNNotificationData(position, args);
        final String merchantSecretKey = retrieveMerchantSecretKeyForNotification(dmnNotificationData);
        final String totalAmount = dmnNotificationData.getTotalAmount();
        final String currency = dmnNotificationData.getCurrency();
        final String responseTimeStamp = dmnNotificationData.getResponseTimeStamp();
        final String pppTransactionId = dmnNotificationData.getPPP_TransactionID();
        final String status = dmnNotificationData.getStatus();
        final String productId = dmnNotificationData.getProductId();

        final String fieldsToHash = merchantSecretKey.concat(totalAmount)
                .concat(currency)
                .concat(responseTimeStamp)
                .concat(pppTransactionId)
                .concat(Optional.ofNullable(status).orElse(StringUtils.EMPTY)
                        .concat(productId));

        LOG.debug("Information to hash for checksum [{}]", fieldsToHash);
        final String hashedString = NuveiHashAlgorithmUtil.getHash(fieldsToHash, StandardCharsets.UTF_8.name(), Constants.HashAlgorithm.SHA256);
        LOG.debug("Information hashed for checksum [{}]", hashedString);

        return Optional.ofNullable(dmnNotificationData.getAdvanceResponseChecksum()).orElse(StringUtils.EMPTY).equals(hashedString);
    }

    /**
     * Retrieves the {@code merchantSecretKey} based on the {@code merchantId} and {@code siteId} of the {@code DMN}
     *
     * @param dmnNotificationData the incoming {@link NuveiIncomingDMNData}
     * @return the secret key
     */
    protected String retrieveMerchantSecretKeyForNotification(final NuveiIncomingDMNData dmnNotificationData) {
        final String merchantId = dmnNotificationData.getMerchant_id();
        final String merchantSiteId = dmnNotificationData.getMerchant_site_id();
        final NuveiMerchantConfigurationModel merchantConfigurationModel = nuveiMerchantConfigurationService.getMerchantConfigurationByMerchantIdAndSiteId(merchantId, merchantSiteId);
        return merchantConfigurationModel.getMerchantSecretKey();
    }

    /**
     * Retrieves the object in {@code position} for the {@code args} and casts it to {@link NuveiIncomingDMNData}
     *
     * @param position the position of the object within the {@code args}
     * @param args     the args
     * @return the object casted to {@link NuveiIncomingDMNData}
     */
    protected NuveiIncomingDMNData getDMNNotificationData(final Integer position, final Object[] args) {
        final Object arg = args[position];
        LOG.debug("Notification received {}", () -> gson.toJson(arg));

        if (arg instanceof NuveiIncomingDMNData) {
            return (NuveiIncomingDMNData) arg;
        }

        return null;
    }


    /**
     * This method identifies all the parameters with type {@link NuveiIncomingDMNData} annotated with {@link NuveiValidateChecksum}
     * and returns the position of them on the method signature
     *
     * @param parameterTypes the {@code} parameterTypes of the method signature
     * @param annotations    The annotations matrix of the method signature
     * @return the {@link Set} with the positions of all parameters that fulfills the conditions
     */
    protected Set<Integer> getParameterIndexesForAnnotatedParam(final Class<?>[] parameterTypes, final Annotation[][] annotations) {
        final Set<Integer> annotatedParamsIndexes = new HashSet<>();
        for (int i = 0; i < annotations.length; i++) {
            for (int j = 0; j < annotations[i].length; j++) {
                if (annotations[i][j].annotationType().equals(NuveiValidateChecksum.class) && parameterTypes[i].equals(NuveiIncomingDMNData.class)) {
                    annotatedParamsIndexes.add(i);
                }
            }
        }
        return annotatedParamsIndexes;
    }
}
