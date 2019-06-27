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
package eu.europa.ec.fisheries.uvms.config.service.bean;

import eu.europa.ec.fisheries.uvms.config.model.mapper.ModuleRequestMapper;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import eu.europa.ec.fisheries.uvms.config.service.message.ConfigMessageProducerBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Startup
public class ConfigStartupAction {

    @EJB
    private ConfigMessageProducerBean producer;

    private static final Logger LOG = LoggerFactory.getLogger(ConfigStartupAction.class);

    @PostConstruct
    protected void sendConfigDeployedMessage() {
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    String message = ModuleRequestMapper.toConfigDeployedMessage();
                    producer.sendConfigDeployedMessage(message);
                } catch (Exception e) {
                    LOG.error("[ Error when sending config deployed message on topic. ] {}", e.getMessage());
                }
            }
        });
    }
}