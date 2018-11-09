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
package eu.europa.ec.fisheries.uvms.config.message.consumer.bean;

import eu.europa.ec.fisheries.uvms.commons.message.api.MessageConstants;
import eu.europa.ec.fisheries.uvms.commons.message.context.MappedDiagnosticContext;
import eu.europa.ec.fisheries.uvms.config.message.event.ErrorEvent;
import eu.europa.ec.fisheries.uvms.config.message.event.EventMessage;
import eu.europa.ec.fisheries.uvms.config.message.event.ConfigMessageRecievedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

@MessageDriven(mappedName = MessageConstants.QUEUE_CONFIG, activationConfig = {
        @ActivationConfigProperty(propertyName = MessageConstants.MESSAGING_TYPE_STR, propertyValue = MessageConstants.CONNECTION_TYPE),
        @ActivationConfigProperty(propertyName = MessageConstants.DESTINATION_TYPE_STR, propertyValue = MessageConstants.DESTINATION_TYPE_QUEUE),
        @ActivationConfigProperty(propertyName = MessageConstants.DESTINATION_STR, propertyValue = MessageConstants.QUEUE_CONFIG_IN_NAME),
        @ActivationConfigProperty(propertyName = MessageConstants.DESTINATION_JNDI_NAME, propertyValue = MessageConstants.QUEUE_CONFIG),
        @ActivationConfigProperty(propertyName = MessageConstants.CONNECTION_FACTORY_JNDI_NAME, propertyValue = MessageConstants.CONNECTION_FACTORY)
})
public class ConfigEventConsumerBean implements MessageListener {

    private static final Logger LOG = LoggerFactory.getLogger(ConfigEventConsumerBean.class);

    @Inject
    @ConfigMessageRecievedEvent
    private Event<EventMessage> messageRecievedEvent;

    @Inject
    @ErrorEvent
    private Event<EventMessage> errorEvent;

    @Override
    public void onMessage(Message message) {
        TextMessage textMessage = (TextMessage) message;
        MappedDiagnosticContext.addMessagePropertiesToThreadMappedDiagnosticContext(textMessage);
        try {
            messageRecievedEvent.fire(new EventMessage(textMessage));
        } catch (NullPointerException e) {
            LOG.error("[ Error when receiving message in config: ]", e);
            errorEvent.fire(new EventMessage(textMessage, "Error when receiving message in config: " + e.getMessage()));
        }
    }

}
