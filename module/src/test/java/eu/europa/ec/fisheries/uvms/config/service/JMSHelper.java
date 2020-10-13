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
package eu.europa.ec.fisheries.uvms.config.service;

import eu.europa.ec.fisheries.uvms.commons.message.api.MessageConstants;

import javax.jms.*;
import java.util.Enumeration;

public class JMSHelper {

    private static final long TIMEOUT = 20000;
    private static final String CONFIG_QUEUE = "UVMSConfigEvent";
    public static final String RESPONSE_QUEUE = "IntegrationTestsResponseQueue";

    private final ConnectionFactory connectionFactory;

    public JMSHelper(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    /*public PingResponse pingMovement() throws Exception {
        String pingRequest = MovementModuleRequestMapper.mapToPingRequest(null);
        String correlationId = sendMovementMessage(pingRequest, null, null);
        message response = listenForResponse(correlationId);
        return JAXBMarshaller.unmarshallTextMessage((TextMessage) response, PingResponse.class);
    }*/


    public String sendConfigMessage(String text, String function) throws Exception {
        Connection connection = connectionFactory.createConnection();
        try {
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Queue responseQueue = session.createQueue(RESPONSE_QUEUE);
            Queue movementQueue = session.createQueue(CONFIG_QUEUE);

            TextMessage message = session.createTextMessage();
            message.setStringProperty(MessageConstants.JMS_FUNCTION_PROPERTY, function);
            message.setText(text);
            message.setJMSReplyTo(responseQueue);

            session.createProducer(movementQueue).send(message);

            return message.getJMSMessageID();
        } finally {
            connection.close();
        }
    }

    public Message listenForResponse(String correlationId) throws Exception {
        Connection connection = connectionFactory.createConnection();
        try {
            connection.start();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Queue responseQueue = session.createQueue(RESPONSE_QUEUE);

            return session.createConsumer(responseQueue, "JMSCorrelationID='" + correlationId + "'")
                          .receive(TIMEOUT);
        } finally {
            connection.close();
        }
    }


    public Message listenOnQueue(String queue) throws Exception {
        Connection connection = connectionFactory.createConnection();
        try {
            connection.start();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Queue responseQueue = session.createQueue(queue);

            return session.createConsumer(responseQueue)
                    .receive(TIMEOUT);
        } finally {
            connection.close();
        }
    }

    public int checkQueueSize(String queue) throws Exception {
        int messages = 0;
        Connection connection = connectionFactory.createConnection();
        try {
            connection.start();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Queue responseQueue = session.createQueue(queue);

            QueueBrowser browser = session.createBrowser(responseQueue);
            
            Enumeration enumeration = browser.getEnumeration();
            while(enumeration.hasMoreElements()) {
                enumeration.nextElement();
                messages++;
            }
        } finally {
            connection.close();
        }
        return messages;
    }

    public void clearQueue(String queue) throws Exception {
        Connection connection = connectionFactory.createConnection();
        MessageConsumer consumer;
        try {
            connection.start();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Queue responseQueue = session.createQueue(queue);
            consumer = session.createConsumer(responseQueue);

            while (consumer.receive(10L) != null);
        } finally {
            connection.close();
        }
    }
}
