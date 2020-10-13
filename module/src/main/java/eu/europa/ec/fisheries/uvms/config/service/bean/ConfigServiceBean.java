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

import eu.europa.ec.fisheries.schema.config.types.v1.SettingType;
import eu.europa.ec.fisheries.schema.config.types.v1.SettingsCatalogEntry;
import eu.europa.ec.fisheries.uvms.audit.model.mapper.AuditLogModelMapper;
import eu.europa.ec.fisheries.uvms.config.model.constants.AuditObjectTypeEnum;
import eu.europa.ec.fisheries.uvms.config.model.constants.AuditOperationEnum;
import eu.europa.ec.fisheries.uvms.config.model.mapper.ModuleRequestMapper;
import eu.europa.ec.fisheries.uvms.config.service.message.ConfigMessageProducerBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.jms.JMSException;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Stateless
public class ConfigServiceBean {

    private static final Logger LOG = LoggerFactory.getLogger(ConfigServiceBean.class);

    @Inject
    private ConfigMessageProducerBean producer;

    @Inject
    private ModuleAvailabilityBean moduleAvailability;
    
    @Inject
    private ConfigDomainModelBean configModel;

    public SettingType create(SettingType setting, String moduleName, String username) {
            SettingType createdSetting = configModel.create(setting, moduleName, username);
            producer.sendConfigDeployedMessage(ModuleRequestMapper.toSetSettingEventRequest(createdSetting));
            sendAuditMessage(AuditOperationEnum.CREATE, createdSetting, username);
            return createdSetting;
    }

    public List<SettingType> createAll(List<SettingType> settings, String moduleName, String username) {
        List<SettingType> createdSettings = configModel.createAll(settings, moduleName, username);
        for (SettingType createdSetting : createdSettings) {
            sendAuditMessage(AuditOperationEnum.CREATE, createdSetting, username);
        }

        return createdSettings;
    }

    public SettingType getById(Long settingId) {
        return configModel.get(settingId);
    }

    public SettingType update(SettingType setting, String username) {
        SettingType updatedSetting = configModel.update(setting, username);
        producer.sendConfigDeployedMessage(ModuleRequestMapper.toSetSettingEventRequest(updatedSetting));
        sendAuditMessage(AuditOperationEnum.UPDATE, updatedSetting, username);
        return updatedSetting;
    }

    public SettingType reset(Long settingId, String username) {
        SettingType deletedSetting = configModel.delete(settingId);
        producer.sendConfigDeployedMessage(ModuleRequestMapper.toResetSettingEventRequest(deletedSetting));
        sendAuditMessage(AuditOperationEnum.DELETE, deletedSetting, username);
        return deletedSetting;
    }

    public SettingType reset(String settingKey, String moduleName, String username) {
        SettingType deletedSetting = configModel.delete(settingKey, moduleName);
        producer.sendConfigDeployedMessage(ModuleRequestMapper.toResetSettingEventRequest(deletedSetting));
        sendAuditMessage(AuditOperationEnum.DELETE, deletedSetting, username);
        return deletedSetting;
    }
    
    public List<SettingType> getListIncludingGlobal(String moduleName) {
        return configModel.getListIncludingGlobal(moduleName);
    }

    public Map<String, List<SettingType>> getCatalog() {
        List<SettingsCatalogEntry> catalog = configModel.getSettingsCatalog();
        return getCatalogFromSettingCatalogResponse(catalog);
    }

    private Map<String, List<SettingType>> getCatalogFromSettingCatalogResponse(List<SettingsCatalogEntry> catalog) {
        Map<String, List<SettingType>> settingsByModule = new HashMap<>();
        for (SettingsCatalogEntry entry : catalog) {
            settingsByModule.put(entry.getModuleName(), entry.getSettings());
        }

        return settingsByModule;
    }

    public void setModuleTimestamp(String moduleName, Instant timestamp) {
        moduleAvailability.setTimestamp(moduleName, timestamp); 
    }

    public Map<String, Instant> getModuleTimestamps() {
        return moduleAvailability.getTimestamps();
    }

    public List<SettingType> getGlobalSettings() {
        return configModel.getGlobalSettings();
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

            String message = AuditLogModelMapper.mapToAuditLog(AuditObjectTypeEnum.SETTING.getValue(), operation.getValue(), affectedObject, userName);
            producer.sendAuditMessage(message);
        }
        catch (JMSException e) {
            LOG.error("[ Error when sending message to Audit. {} {} {} ] {}",operation,setting,userName, e.getMessage());
        }
    }

}