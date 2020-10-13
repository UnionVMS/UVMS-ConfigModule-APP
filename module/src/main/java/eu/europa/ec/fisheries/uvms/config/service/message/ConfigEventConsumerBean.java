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

import eu.europa.ec.fisheries.schema.config.module.v1.*;
import eu.europa.ec.fisheries.schema.config.types.v1.PullSettingsStatus;
import eu.europa.ec.fisheries.schema.config.types.v1.SettingType;
import eu.europa.ec.fisheries.uvms.commons.message.api.MessageConstants;
import eu.europa.ec.fisheries.uvms.commons.message.context.MappedDiagnosticContext;
import eu.europa.ec.fisheries.uvms.config.model.mapper.JAXBMarshaller;
import eu.europa.ec.fisheries.uvms.config.model.mapper.ModuleResponseMapper;
import eu.europa.ec.fisheries.uvms.config.service.bean.ConfigServiceBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@MessageDriven(activationConfig = {
        @ActivationConfigProperty(propertyName = MessageConstants.DESTINATION_TYPE_STR, propertyValue = MessageConstants.DESTINATION_TYPE_QUEUE),
        @ActivationConfigProperty(propertyName = MessageConstants.DESTINATION_LOOKUP_STR, propertyValue = MessageConstants.QUEUE_CONFIG),
})
public class ConfigEventConsumerBean implements MessageListener {

    private static final Logger LOG = LoggerFactory.getLogger(ConfigEventConsumerBean.class);

    @Inject
    ConfigMessageProducerBean messageProducer;

    @Inject
    private ConfigServiceBean service;

    @Override
    public void onMessage(Message message) {
        TextMessage textMessage = (TextMessage) message;
        MappedDiagnosticContext.addMessagePropertiesToThreadMappedDiagnosticContext(textMessage);
        try {
            LOG.trace("Message received in config");
            String responseMessage;

            String methodString = textMessage.getStringProperty(MessageConstants.JMS_FUNCTION_PROPERTY);
            if(methodString == null){
                ConfigModuleBaseRequest baseRequest = JAXBMarshaller.unmarshallTextMessage(textMessage, ConfigModuleBaseRequest.class);
                methodString = baseRequest.getMethod().toString();
            }
            ConfigModuleMethod method = ConfigModuleMethod.fromValue(methodString);
            LOG.info("[INFO] Received message with following ConfigModuleMethod : {}", method);

            switch (method) {
                case PULL:
                    PullSettingsRequest pullRequest = JAXBMarshaller.unmarshallTextMessage(textMessage, PullSettingsRequest.class);
                    LOG.info("[INFO] Going to fetch the settings related to module : {}", pullRequest.getModuleName());
                    List<SettingType> settings = service.getListIncludingGlobal(pullRequest.getModuleName());
                    if (settings == null) {
                        responseMessage = ModuleResponseMapper.toPullSettingsResponse(new ArrayList<SettingType>(), PullSettingsStatus.MISSING);
                    } else {
                        responseMessage = ModuleResponseMapper.toPullSettingsResponse(settings, PullSettingsStatus.OK);
                    }
                    messageProducer.sendResponseMessageToSender(textMessage, responseMessage);
                    break;
                case PUSH:
                    PushSettingsRequest pushRequest = JAXBMarshaller.unmarshallTextMessage(textMessage, PushSettingsRequest.class);
                    List<SettingType> localSettings = getLocalSettings(pushRequest.getSettings());
                    List<SettingType> createdSettings = service.createAll(localSettings, pushRequest.getModuleName(), pushRequest.getUsername());
                    createdSettings.addAll(service.getGlobalSettings());
                    responseMessage = ModuleResponseMapper.toPushSettingsResponse(createdSettings);
                    messageProducer.sendResponseMessageToSender(textMessage, responseMessage);
                    break;
                case SET:
                    SetSettingRequest setRequest = JAXBMarshaller.unmarshallTextMessage(textMessage, SetSettingRequest.class);
                    SettingType createdSetting = service.create(setRequest.getSetting(), setRequest.getModule(), setRequest.getUsername());
                    responseMessage = ModuleResponseMapper.toSingleSettingResponse(createdSetting);
                    messageProducer.sendResponseMessageToSender(textMessage, responseMessage);
                    break;
                case RESET:
                    ResetSettingRequest resetRequest = JAXBMarshaller.unmarshallTextMessage(textMessage, ResetSettingRequest.class);
                    SettingType deletedSetting;
                    if (resetRequest.getSetting().getId() != null) {
                        deletedSetting = service.reset(resetRequest.getSetting().getId(), resetRequest.getUsername());
                    } else {
                        deletedSetting = service.reset(resetRequest.getSetting().getKey(), resetRequest.getSetting().getModule(), resetRequest.getUsername());
                    }
                    responseMessage = ModuleResponseMapper.toSingleSettingResponse(deletedSetting);
                    messageProducer.sendResponseMessageToSender(textMessage, responseMessage);
                    break;
                case PING:
                    PingRequest pingRequest = JAXBMarshaller.unmarshallTextMessage(textMessage, PingRequest.class);
                    service.setModuleTimestamp(pingRequest.getModuleName(), Instant.ofEpochMilli(textMessage.getJMSTimestamp()));
                    break;
                case LIST:
                    ListSettingsRequest listRequest = JAXBMarshaller.unmarshallTextMessage(textMessage, ListSettingsRequest.class);
                    List<SettingType> list = service.getListIncludingGlobal(listRequest.getModuleName());
                    if (list == null) {
                        responseMessage = ModuleResponseMapper.toSettingsListResponse(new ArrayList<SettingType>());
                    } else {
                        responseMessage = ModuleResponseMapper.toSettingsListResponse(list);
                    }
                    messageProducer.sendResponseMessageToSender(textMessage, responseMessage);
                    break;
                default:
                    break;
                }
        } catch (Exception e) {
            LOG.error("[ Error when receiving message in config: ]", e);
            messageProducer.sendModuleErrorMessage(textMessage, "Error when receiving message in config: " + e.getMessage());
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
}
