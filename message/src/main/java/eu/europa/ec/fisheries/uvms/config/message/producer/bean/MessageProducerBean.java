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
package eu.europa.ec.fisheries.uvms.config.message.producer.bean;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.enterprise.event.Observes;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.europa.ec.fisheries.uvms.commons.message.impl.JMSUtils;
import eu.europa.ec.fisheries.uvms.config.message.constants.DataSourceQueue;
import eu.europa.ec.fisheries.uvms.config.message.constants.MessageConstants;
import eu.europa.ec.fisheries.uvms.config.message.event.ErrorEvent;
import eu.europa.ec.fisheries.uvms.config.message.event.EventMessage;
import eu.europa.ec.fisheries.uvms.config.message.exception.MessageException;
import eu.europa.ec.fisheries.uvms.config.message.producer.MessageProducer;
import eu.europa.ec.fisheries.uvms.config.model.exception.ModelMarshallException;
import eu.europa.ec.fisheries.uvms.config.model.mapper.JAXBMarshaller;

@Singleton
public class MessageProducerBean implements MessageProducer {

    final static Logger LOG = LoggerFactory.getLogger(MessageProducerBean.class);

    private Queue auditQueue;
    private Queue responseQueue;

    private Topic configTopic;

    private ConnectionFactory connectionFactory;

    @PostConstruct
    public void init() {
    	connectionFactory = JMSUtils.lookupConnectionFactory();
    	responseQueue = JMSUtils.lookupQueue(MessageConstants.COMPONENT_RESPONSE_QUEUE);
        auditQueue = JMSUtils.lookupQueue(MessageConstants.AUDIT_MODULE_QUEUE);
        configTopic = JMSUtils.lookupTopic(MessageConstants.CONFIG_STATUS_TOPIC);
    }

    
    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public String sendDataSourceMessage(String text, DataSourceQueue queue) throws MessageException {        
    	Connection connection=null;
    	try {
    		
    		connection = connectionFactory.createConnection();
            final Session session = JMSUtils.connectToQueue(connection);
            
            TextMessage message = session.createTextMessage();
            message.setJMSReplyTo(responseQueue);
            message.setText(text);

            switch (queue) {
                case AUDIT:
                    getProducer(session, auditQueue).send(message);
                    break;
                default:
                	break;
            }

            return message.getJMSMessageID();
        } catch (Exception e) {
            LOG.error("[ Error when sending message. ] {0}", e.getMessage());
            throw new MessageException("[ Error when sending message. ]", e);
        } finally {
        	JMSUtils.disconnectQueue(connection);
        }
    }

    public void sendConfigDeployedMessage(String text) throws MessageException {
    	Connection connection=null;
    	try {
    		connection = connectionFactory.createConnection();
            final Session session = JMSUtils.connectToQueue(connection);
            TextMessage message = session.createTextMessage(text);
            getProducer(session, configTopic).send(message);
        }
        catch (Exception e) {
            LOG.error("[ Error when sending config deployed message. ] {}", e.getMessage());
            throw new MessageException("Error when sending config deployed message.", e);
        }
        finally {
        	JMSUtils.disconnectQueue(connection);
        }
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void sendModuleResponseMessage(TextMessage requestMessage, String response) throws MessageException {
        sendReply(requestMessage, response);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void sendModuleErrorMessage(@Observes @ErrorEvent EventMessage eventMessage) throws MessageException {
        try {
            String faultString = JAXBMarshaller.marshallJaxBObjectToString(eventMessage.getFault());
            sendReply(eventMessage.getJmsMessage(), faultString);
        } catch (ModelMarshallException e) {
            LOG.error("[ Error when sending module error message. ] {}", e.getMessage());
            throw new MessageException("[ Error when sending module error message. ]", e);
        }
    }

    private void sendReply(TextMessage requestMessage, String response) throws MessageException {
    	Connection connection=null;
    	try {
    		connection = connectionFactory.createConnection();
            final Session session = JMSUtils.connectToQueue(connection);
            TextMessage responseMessage = session.createTextMessage(response);
            responseMessage.setJMSCorrelationID(requestMessage.getJMSMessageID());
            getProducer(session, requestMessage.getJMSReplyTo()).send(responseMessage);
        }
        catch (JMSException e) {
            LOG.error("[ Error when replying to module message. ] {}", e.getMessage());
            throw new MessageException("Error when replying to module message.", e);
        }
        finally {
        	JMSUtils.disconnectQueue(connection);
        }        
    }


    private javax.jms.MessageProducer getProducer(Session session, Destination destination) throws JMSException {
        javax.jms.MessageProducer producer = session.createProducer(destination);
        producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        producer.setTimeToLive(60000L);
        return producer;
    }
}