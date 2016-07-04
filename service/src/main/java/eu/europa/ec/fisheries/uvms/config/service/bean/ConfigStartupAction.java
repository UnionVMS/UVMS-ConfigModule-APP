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
package eu.europa.ec.fisheries.uvms.config.service.bean;

import javax.annotation.PostConstruct;
import javax.ejb.DependsOn;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.europa.ec.fisheries.uvms.config.message.producer.MessageProducer;
import eu.europa.ec.fisheries.uvms.config.model.mapper.ModuleRequestMapper;

@Singleton
@Startup
@DependsOn(value = { "MessageProducerBean" })
public class ConfigStartupAction {

    @EJB
    MessageProducer producer;

    final static Logger LOG = LoggerFactory.getLogger(ConfigStartupAction.class);

    @PostConstruct
    protected void sendConfigDeployedMessage() {
        try {
            String message = ModuleRequestMapper.toConfigDeployedMessage();
            producer.sendConfigDeployedMessage(message);
        } catch (Exception e) {
            LOG.error("[ Error when sending config deployed message on topic. ] {}", e.getMessage());
        }
    }
}