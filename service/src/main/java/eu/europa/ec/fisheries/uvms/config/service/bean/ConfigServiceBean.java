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

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.TextMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.europa.ec.fisheries.schema.config.types.v1.SettingType;
import eu.europa.ec.fisheries.uvms.audit.model.exception.AuditModelMarshallException;
import eu.europa.ec.fisheries.uvms.audit.model.mapper.AuditLogMapper;
import eu.europa.ec.fisheries.uvms.config.message.constants.DataSourceQueue;
import eu.europa.ec.fisheries.uvms.config.message.consumer.MessageConsumer;
import eu.europa.ec.fisheries.uvms.config.message.exception.MessageException;
import eu.europa.ec.fisheries.uvms.config.message.producer.MessageProducer;
import eu.europa.ec.fisheries.uvms.config.model.constants.AuditObjectTypeEnum;
import eu.europa.ec.fisheries.uvms.config.model.constants.AuditOperationEnum;
import eu.europa.ec.fisheries.uvms.config.model.exception.ModelMapperException;
import eu.europa.ec.fisheries.uvms.config.model.mapper.ConfigDataSourceRequestMapper;
import eu.europa.ec.fisheries.uvms.config.model.mapper.ConfigDataSourceResponseMapper;
import eu.europa.ec.fisheries.uvms.config.model.mapper.ModuleRequestMapper;
import eu.europa.ec.fisheries.uvms.config.service.ConfigService;
import eu.europa.ec.fisheries.uvms.config.service.ParameterService;
import eu.europa.ec.fisheries.uvms.config.service.exception.ServiceException;

@Stateless
public class ConfigServiceBean implements ConfigService {

    final static Logger LOG = LoggerFactory.getLogger(ConfigServiceBean.class);

    @EJB
    ParameterService parameterService;

    @EJB
    MessageConsumer consumer;

    @EJB
    MessageProducer producer;

    @Inject
    ModuleAvailabilityBean moduleAvailability;

    @Override
    public SettingType create(SettingType setting, String moduleName, String username) throws ServiceException {
        LOG.info("Create setting invoked in service layer.");
        try {
            String request = ConfigDataSourceRequestMapper.toCreateSettingRequest(setting, moduleName, username);
            String messageId = producer.sendDataSourceMessage(request, DataSourceQueue.INTERNAL);
            TextMessage response = consumer.getMessage(messageId, TextMessage.class);
            SettingType createdSetting = ConfigDataSourceResponseMapper.getSettingFromCreateSettingResponse(response, messageId);
            producer.sendConfigDeployedMessage(ModuleRequestMapper.toSetSettingEventRequest(createdSetting));
            sendAuditMessage(AuditOperationEnum.CREATE, createdSetting, username);
            return createdSetting;
        }
        catch (ModelMapperException | MessageException e) {
            LOG.error("[ Error when creating setting. ] {}", e.getMessage());
            throw new ServiceException(e.getMessage());
        }
    }

    @Override
    public List<SettingType> createAll(List<SettingType> settings, String moduleName, String username) throws ServiceException {
        try {
            String request = ConfigDataSourceRequestMapper.toCreateAllSettingsRequest(settings, moduleName, username);
            String messageId = producer.sendDataSourceMessage(request, DataSourceQueue.INTERNAL);
            TextMessage response = consumer.getMessage(messageId, TextMessage.class);
            List<SettingType> createdSettings = ConfigDataSourceResponseMapper.getSettingsFromListSettingsResponse(response, messageId);
            for (SettingType createdSetting : createdSettings) {
                sendAuditMessage(AuditOperationEnum.CREATE, createdSetting, username);
            }

            return createdSettings;
        }
        catch (ModelMapperException | MessageException e) {
            LOG.error("[ Error when creating settings. ] {}", e.getMessage());
            throw new ServiceException(e.getMessage());
        }
    }

    @Override
    public SettingType getById(Long settingId) throws ServiceException {
        LOG.info("Get setting by ID invoked in service layer.");
        try {
            String request = ConfigDataSourceRequestMapper.toGetSettingRequest(settingId);
            String messageId = producer.sendDataSourceMessage(request, DataSourceQueue.INTERNAL);
            TextMessage response = consumer.getMessage(messageId, TextMessage.class);
            return ConfigDataSourceResponseMapper.getSettingFromGetSettingResponse(response, messageId);
        }
        catch (ModelMapperException | MessageException e) {
            LOG.error("[ Error when getting setting by ID. ] {}", e.getMessage());
            throw new ServiceException(e.getMessage());
        }
    }

    @Override
    public SettingType update(Long settingId, SettingType setting, String username) throws ServiceException {
        LOG.info("Update setting invoked in service layer.");
        try {
            String request = ConfigDataSourceRequestMapper.toUpdateSettingRequest(settingId, setting, username);
            String messageId = producer.sendDataSourceMessage(request, DataSourceQueue.INTERNAL);
            TextMessage response = consumer.getMessage(messageId, TextMessage.class);
            SettingType updatedSetting = ConfigDataSourceResponseMapper.getSettingFromUpdateSettingResponse(response, messageId);
            producer.sendConfigDeployedMessage(ModuleRequestMapper.toSetSettingEventRequest(updatedSetting));
            sendAuditMessage(AuditOperationEnum.UPDATE, updatedSetting, username);
            return updatedSetting;
        }
        catch (ModelMapperException | MessageException e) {
            LOG.error("[ Error when updating setting. ] {}", e.getMessage());
            throw new ServiceException(e.getMessage());
        }
    }

    @Override
    public SettingType delete(Long settingId, String username) throws ServiceException {
        LOG.info("Delete setting invoked in service layer.");
        try {
            return delete(ConfigDataSourceRequestMapper.toDeleteSettingRequest(settingId), username);
        }
        catch (ModelMapperException | MessageException | JMSException ex) {
            LOG.error("[ Error when deleting setting. ] {}", ex.getMessage());
            throw new ServiceException(ex.getMessage());
        }
    }

    @Override
    public SettingType delete(String settingKey, String moduleName, String username) throws ServiceException {
        LOG.info("Delete setting invoked in service layer.");
        try {
            return delete(ConfigDataSourceRequestMapper.toDeleteSettingRequest(settingKey, moduleName), username);
        }
        catch (ModelMapperException | MessageException | JMSException ex) {
            LOG.error("[ Error when deleting setting. ] {}", ex.getMessage());
            throw new ServiceException(ex.getMessage());
        }
    }

    private SettingType delete(String deleteRequest, String username) throws MessageException, ModelMapperException, JMSException {
        String messageId = producer.sendDataSourceMessage(deleteRequest, DataSourceQueue.INTERNAL);
        TextMessage response = consumer.getMessage(messageId, TextMessage.class);
        SettingType deletedSetting = ConfigDataSourceResponseMapper.getSettingFromDeleteSettingResponse(response, messageId);
        producer.sendConfigDeployedMessage(ModuleRequestMapper.toResetSettingEventRequest(deletedSetting));
        sendAuditMessage(AuditOperationEnum.DELETE, deletedSetting, username);
        return deletedSetting;
    }
    
    @Override
    public List<SettingType> getList(String moduleName) throws ServiceException {
        LOG.info("List invoked in service layer");
        try {
            String request = ConfigDataSourceRequestMapper.toListSettingsRequest(moduleName);
            String messageId = producer.sendDataSourceMessage(request, DataSourceQueue.INTERNAL);
            TextMessage response = consumer.getMessage(messageId, TextMessage.class);
            return ConfigDataSourceResponseMapper.getSettingsFromListSettingsResponse(response, messageId);
        }
        catch (ModelMapperException | MessageException e) {
            LOG.error("[ Error when getting settings list for module {}. ] {}", moduleName, e);
            throw new ServiceException(e.getMessage());
        }
    }

    @Override
    public Map<String, List<SettingType>> getCatalog() throws ServiceException {
        try {
            String request = ConfigDataSourceRequestMapper.toGetSettingsCatalogRequest();
            String messageId = producer.sendDataSourceMessage(request, DataSourceQueue.INTERNAL);
            TextMessage response = consumer.getMessage(messageId, TextMessage.class);
            return ConfigDataSourceResponseMapper.getCatalogFromSettingCatalogResponse(response, messageId);
        }
        catch (ModelMapperException | MessageException e) {
            LOG.error("[ Error when getting settings catalog. ] {}", e.getMessage());
            throw new ServiceException(e.getMessage());
        }
    }

    @Override
    public void setModuleTimestamp(String moduleName, Date timestamp) {
        moduleAvailability.setTimestamp(moduleName, timestamp); 
    }

    @Override
    public Map<String, Date> getModuleTimestamps() {
        return moduleAvailability.getTimestamps();
    }

    @Override
    public List<SettingType> getGlobalSettings() throws ServiceException {
        try {
            String request = ConfigDataSourceRequestMapper.toGetGlobalSettingsRequest();
            String messageId = producer.sendDataSourceMessage(request, DataSourceQueue.INTERNAL);
            TextMessage response = consumer.getMessage(messageId, TextMessage.class);
            return ConfigDataSourceResponseMapper.getSettingsFromListSettingsResponse(response, messageId);
        } catch (MessageException | ModelMapperException e) {
            LOG.error("[ Error when getting global settings. ] {}", e.getMessage());
            throw new ServiceException(e.getMessage());
        }
    }

    private void sendAuditMessage(AuditOperationEnum operation, SettingType setting, String userName) {
        try {
            String affectedObject;
            if (setting.getModule() != null) {
                // Module setting
                affectedObject = setting.getModule() + "/" + setting.getKey();
            }
            else {
                // Global setting
                affectedObject = setting.getKey();
            }

            String message = AuditLogMapper.mapToAuditLog(AuditObjectTypeEnum.SETTING.getValue(), operation.getValue(), affectedObject, userName);
            producer.sendDataSourceMessage(message, DataSourceQueue.AUDIT);
        }
        catch (AuditModelMarshallException | MessageException e) {
            LOG.error("[ Error when sending message to Audit. ] {}", e.getMessage());
        }
    }

}