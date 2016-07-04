/*
﻿﻿Developed with the contribution of the European Commission - Directorate General for Maritime Affairs and Fisheries
© European Union, 2015-2016.
 
This file is part of the Integrated Data Fisheries Management (IFDM) Suite. The IFDM Suite is free software: you can
redistribute it and/or modify it under the terms of the GNU General Public License as published by the
Free Software Foundation, either version 3 of the License, or any later version. The IFDM Suite is distributed in
the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details. You should have received a copy
of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.config.message.event;

import javax.jms.TextMessage;

import eu.europa.ec.fisheries.schema.config.types.v1.ConfigFault;

public class EventMessage {

    private TextMessage jmsMessage;
    private String errorMessage;
    private ConfigFault configFault;

    public EventMessage(TextMessage jmsMessage) {
        this.jmsMessage = jmsMessage;
    }

    public EventMessage(TextMessage jmsMessage, String errorMessage) {
        this.jmsMessage = jmsMessage;
        this.errorMessage = errorMessage;
    }

    public EventMessage(TextMessage jmsMessage, ConfigFault configFault) {
        this.jmsMessage = jmsMessage;
        this.configFault = configFault;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public TextMessage getJmsMessage() {
        return jmsMessage;
    }

    public void setJmsMessage(TextMessage jmsMessage) {
        this.jmsMessage = jmsMessage;
    }

    public ConfigFault getFault() {
        return configFault;
    }

    public void setFault(ConfigFault fault) {
        this.configFault = fault;
    }

}