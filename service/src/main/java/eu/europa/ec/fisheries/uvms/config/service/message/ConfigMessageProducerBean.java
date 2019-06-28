/*
﻿Developed with the contribution of the European Commission - Directorate General for Maritime Affairs and Fisheries
© European Union, 2015-2016.

This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can
redistribute it and/or modify it under the terms of the GNU General Public License as published by the
Free Software Foundation, either version 3 of the License, or any later version. The IFDM Suite is distributed in
the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details. You should have received a
copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.config.service.message;

import eu.europa.ec.fisheries.uvms.commons.message.impl.AbstractProducer;
import eu.europa.ec.fisheries.uvms.config.model.mapper.JAXBMarshaller;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.TextMessage;

@Stateless
@LocalBean
public class ConfigMessageProducerBean extends AbstractProducer {

    private static final Logger LOG = LoggerFactory.getLogger(ConfigMessageProducerBean.class);

    @EJB
    private ConfigMessageConsumerBean configConsumer;

    @EJB
    private AuditQueueProducerBean auditProducer;

    @EJB
    private ConfigTopicProducer configTopicProducer;

    public String sendAuditMessage(String text) throws JMSException {
        return auditProducer.sendModuleMessage(text, configConsumer.getDestination());
    }

    public void sendConfigDeployedMessage(String text) {
        try {
            configTopicProducer.sendEventBusMessage(text, StringUtils.EMPTY);
        }catch (JMSException e){
            throw new RuntimeException(e);
        }
    }

    public void sendModuleErrorMessage(TextMessage message, String error) {
        try {
            String faultString = JAXBMarshaller.marshallJaxBObjectToString(error);
            this.sendResponseMessageToSender(message, faultString, 60000, DeliveryMode.NON_PERSISTENT);
        } catch (Exception e) {
            LOG.error("Error when sending module error message.", e);
        }
    }

    @Override
    public Destination getDestination() {
        return null;
    }
}