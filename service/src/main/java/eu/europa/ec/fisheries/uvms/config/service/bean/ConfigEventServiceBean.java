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

import eu.europa.ec.fisheries.schema.config.module.v1.*;
import eu.europa.ec.fisheries.schema.config.types.v1.ConfigFault;
import eu.europa.ec.fisheries.schema.config.types.v1.PullSettingsStatus;
import eu.europa.ec.fisheries.schema.config.types.v1.SettingType;
import eu.europa.ec.fisheries.uvms.commons.message.api.MessageException;
import eu.europa.ec.fisheries.uvms.config.message.event.ErrorEvent;
import eu.europa.ec.fisheries.uvms.config.message.event.EventMessage;
import eu.europa.ec.fisheries.uvms.config.message.event.ConfigMessageRecievedEvent;
import eu.europa.ec.fisheries.uvms.config.message.producer.bean.ConfigMessageProducerBean;
import eu.europa.ec.fisheries.uvms.config.model.exception.ModelMapperException;
import eu.europa.ec.fisheries.uvms.config.model.mapper.JAXBMarshaller;
import eu.europa.ec.fisheries.uvms.config.model.mapper.ModuleResponseMapper;
import eu.europa.ec.fisheries.uvms.config.service.ConfigService;
import eu.europa.ec.fisheries.uvms.config.service.EventService;
import eu.europa.ec.fisheries.uvms.config.service.exception.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.TextMessage;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Stateless
public class ConfigEventServiceBean implements EventService {

    private static final Logger LOG = LoggerFactory.getLogger(ConfigEventServiceBean.class);

    @EJB
    private ConfigMessageProducerBean configMessageProducer;

    @Inject
    @ErrorEvent
    private Event<EventMessage> errorEvent;

    @EJB
    private ConfigService configService;

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void getData(@Observes @ConfigMessageRecievedEvent EventMessage message) {
        TextMessage jmsMessage = message.getJmsMessage();
        try {
            ConfigModuleBaseRequest baseRequest = JAXBMarshaller.unmarshallTextMessage(jmsMessage, ConfigModuleBaseRequest.class);
            String responseMessage;
            LOG.info("[START] Received message with following ConfigModuleMethod : {}",baseRequest.getMethod());
            switch (baseRequest.getMethod()) {
                case PULL:
                    PullSettingsRequest pullRequest = JAXBMarshaller.unmarshallTextMessage(jmsMessage, PullSettingsRequest.class);
                    String moduleName = pullRequest.getModuleName();
                    List<SettingType> settings = configService.getList(moduleName);
                    if (settings == null) {
                        responseMessage = ModuleResponseMapper.toPullSettingsResponse(new ArrayList<>(), PullSettingsStatus.MISSING);
                    } else {
                        responseMessage = ModuleResponseMapper.toPullSettingsResponse(settings, PullSettingsStatus.OK);
                    }
                    configMessageProducer.sendResponseMessageToSender(jmsMessage, responseMessage);
                    LOG.info("[END] Settings sent back to module : [{}]", moduleName);
                    break;
                case PUSH:
                    PushSettingsRequest pushRequest = JAXBMarshaller.unmarshallTextMessage(jmsMessage, PushSettingsRequest.class);
                    List<SettingType> localSettings = getLocalSettings(pushRequest.getSettings());
                    List<SettingType> createdSettings = configService.createAll(localSettings, pushRequest.getModuleName(), baseRequest.getUsername());
                    createdSettings.addAll(configService.getGlobalSettings());
                    responseMessage = ModuleResponseMapper.toPushSettingsResponse(createdSettings);
                    configMessageProducer.sendResponseMessageToSender(jmsMessage, responseMessage);
                    LOG.info("[END] PushSettingsResponse sent back to module : [{}]", pushRequest.getModuleName());
                    break;
                case SET:
                    SetSettingRequest setRequest = JAXBMarshaller.unmarshallTextMessage(jmsMessage, SetSettingRequest.class);
                    SettingType createdSetting = configService.create(setRequest.getSetting(), setRequest.getModule(), baseRequest.getUsername());
                    responseMessage = ModuleResponseMapper.toSingleSettingResponse(createdSetting);
                    configMessageProducer.sendResponseMessageToSender(jmsMessage, responseMessage);
                    LOG.info("[END] SetSettingResponse sent back to module : [{}]", setRequest.getModule());
                    break;
                case RESET:
                    ResetSettingRequest resetRequest = JAXBMarshaller.unmarshallTextMessage(jmsMessage, ResetSettingRequest.class);
                    SettingType deletedSetting;
                    if (resetRequest.getSetting().getId() != null) {
                        deletedSetting = configService.delete(resetRequest.getSetting().getId(), baseRequest.getUsername());
                    } else {
                        deletedSetting = configService.delete(resetRequest.getSetting().getKey(), resetRequest.getSetting().getModule(), baseRequest.getUsername());
                    }
                    responseMessage = ModuleResponseMapper.toSingleSettingResponse(deletedSetting);
                    configMessageProducer.sendResponseMessageToSender(jmsMessage, responseMessage);
                    LOG.info("[END] ResetSettingResponse sent back to module..");
                    break;
                case PING:
                    PingRequest pingRequest = JAXBMarshaller.unmarshallTextMessage(jmsMessage, PingRequest.class);
                    configService.setModuleTimestamp(pingRequest.getModuleName(), new Date(jmsMessage.getJMSTimestamp()));
                    break;
                case LIST:
                    ListSettingsRequest listRequest = JAXBMarshaller.unmarshallTextMessage(jmsMessage, ListSettingsRequest.class);
                    List<SettingType> list = configService.getList(listRequest.getModuleName());
                    if (list == null) {
                        responseMessage = ModuleResponseMapper.toSettingsListResponse(new ArrayList<>());
                    } else {
                        responseMessage = ModuleResponseMapper.toSettingsListResponse(list);
                    }
                    configMessageProducer.sendResponseMessageToSender(jmsMessage, responseMessage);
                    LOG.info("[END] ListSettingsResponse sent back to module : [{}]", listRequest.getModuleName());
                    break;
                default:
                    break;
            }
        } catch (MessageException | ModelMapperException | ServiceException | JMSException e) {
            LOG.error("[ERROR] Error when receiving  config request. {}", e.getMessage());
            errorEvent.fire(new EventMessage(jmsMessage, createFault(e.getMessage())));
        }
    }

    private List<SettingType> getLocalSettings(List<SettingType> settings) {
        List<SettingType> localSettings = new ArrayList<>();
        for (SettingType setting : settings) {
            if (!setting.isGlobal()) {
                localSettings.add(setting);
            }
        }
        return localSettings;
    }

    private ConfigFault createFault(String message) {
        ConfigFault fault = new ConfigFault();
        fault.setMessage(message);
        return fault;
    }

}