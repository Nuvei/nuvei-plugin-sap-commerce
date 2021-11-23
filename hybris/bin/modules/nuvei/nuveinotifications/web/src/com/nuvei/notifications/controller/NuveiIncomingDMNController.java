package com.nuvei.notifications.controller;

import com.nuvei.notifications.checksum.NuveiValidateChecksum;
import com.nuvei.notifications.data.NuveiIncomingDMNData;
import com.nuvei.notifications.facades.NuveiDMNFacade;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.MediaType;

/**
 * Controller that receives Direct Merchant Notifications
 */
@Controller
@RequestMapping(value = "/notifications")
public class NuveiIncomingDMNController {
    private static final Logger LOG = LogManager.getLogger(NuveiIncomingDMNController.class);

    @Resource(name = "nuveiDMNFacade")
    private NuveiDMNFacade nuveiDMNFacade;

    /**
     * Receive notification and if is correct notification is stored into data base
     *
     * @param request          the request
     * @param notificationBody the notification received converted from json
     */
    @ResponseStatus(HttpStatus.OK)
    @PostMapping
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public void receiveNotification(final HttpServletRequest request, @NuveiValidateChecksum final NuveiIncomingDMNData notificationBody) {
        LOG.info("Nuvei notification received with transaction id {}", notificationBody.getPPP_TransactionID());
        nuveiDMNFacade.createAndSaveDMN(notificationBody);
    }
}
