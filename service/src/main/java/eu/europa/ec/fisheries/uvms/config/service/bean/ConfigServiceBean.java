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
import eu.europa.ec.fisheries.uvms.audit.model.exception.AuditModelMarshallException;
import eu.europa.ec.fisheries.uvms.audit.model.mapper.AuditLogMapper;
import eu.europa.ec.fisheries.uvms.commons.message.api.MessageException;
import eu.europa.ec.fisheries.uvms.config.ConfigDomainModel;
import eu.europa.ec.fisheries.uvms.config.message.producer.bean.ConfigMessageProducerBean;
import eu.europa.ec.fisheries.uvms.config.model.constants.AuditObjectTypeEnum;
import eu.europa.ec.fisheries.uvms.config.model.constants.AuditOperationEnum;
import eu.europa.ec.fisheries.uvms.config.model.exception.ConfigModelException;
import eu.europa.ec.fisheries.uvms.config.model.exception.ModelMapperException;
import eu.europa.ec.fisheries.uvms.config.model.mapper.ModuleRequestMapper;
import eu.europa.ec.fisheries.uvms.config.service.ConfigService;
import eu.europa.ec.fisheries.uvms.config.service.DeployedModuleCatalog;
import eu.europa.ec.fisheries.uvms.config.service.exception.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Stateless
public class ConfigServiceBean implements ConfigService {

    final static Logger LOG = LoggerFactory.getLogger(ConfigServiceBean.class);

    private static final String MODULE_NAME = "config";
    private static final String MODULE_VERSION_PROPERTIES_KEY = "uvms.module.version";

    @EJB
    private ConfigMessageProducerBean producer;
    
    @EJB
    private ConfigDomainModel configModel;

    @Inject
    private DeployedModuleCatalog deployedModuleCatalog;

    @Inject
    private PropertiesBean propertiesBean;

    @PostConstruct
    void init() {
        deployedModuleCatalog.setModuleVersion(MODULE_NAME, propertiesBean.getProperty(MODULE_VERSION_PROPERTIES_KEY));
    }

    @Override
    public SettingType create(SettingType setting, String moduleName, String username) throws ServiceException {
        try {
            SettingType createdSetting = configModel.create(setting, moduleName, username);
            producer.sendConfigDeployedMessage(ModuleRequestMapper.toSetSettingEventRequest(createdSetting));
            sendAuditMessage(AuditOperationEnum.CREATE, createdSetting, username);
            return createdSetting;
        }
        catch (ModelMapperException | eu.europa.ec.fisheries.uvms.commons.message.api.MessageException | ConfigModelException e) {
            throw new ServiceException("Error when creating setting: "+setting,e);
        }
    }

    @Override
    public List<SettingType> createAll(List<SettingType> settings, String moduleName, String username) throws ServiceException {
        try {
            List<SettingType> createdSettings = configModel.createAll(settings, moduleName, username);
            for (SettingType createdSetting : createdSettings) {
                sendAuditMessage(AuditOperationEnum.CREATE, createdSetting, username);
            }

            return createdSettings;
        }
        catch (ConfigModelException e) {
            throw new ServiceException("Error when creating setting: " + settings + " " + moduleName + " " +username,e);
        }
    }

    @Override
    public SettingType getById(Long settingId) throws ServiceException {
        try {
            return configModel.get(settingId);
        }
        catch (ConfigModelException e) {
            throw new ServiceException("Error when getting setting by ID: " + settingId, e);
        }
    }

    @Override
    public SettingType update(Long settingId, SettingType setting, String username) throws ServiceException {
        try {
            SettingType updatedSetting = configModel.update(setting, username);
            producer.sendConfigDeployedMessage(ModuleRequestMapper.toSetSettingEventRequest(updatedSetting));
            sendAuditMessage(AuditOperationEnum.UPDATE, updatedSetting, username);
            return updatedSetting;
        }
        catch (ModelMapperException | MessageException | ConfigModelException e) {
            throw new ServiceException(" Error when updating setting with id: " + settingId + " setting: " + setting + " username: " + username, e);
        }
    }

    @Override
    public SettingType delete(Long settingId, String username) throws ServiceException {
        try {
            SettingType deletedSetting = configModel.delete(settingId);
            producer.sendConfigDeployedMessage(ModuleRequestMapper.toResetSettingEventRequest(deletedSetting));
            sendAuditMessage(AuditOperationEnum.DELETE, deletedSetting, username);
            return deletedSetting;
        }
        catch (ModelMapperException | MessageException | ConfigModelException ex) {
            throw new ServiceException("Error when deleting setting: " + settingId + " username: "+ username ,ex);
        }
    }

    @Override
    public SettingType delete(String settingKey, String moduleName, String username) throws ServiceException {
        try {
            SettingType deletedSetting = configModel.delete(settingKey, moduleName);
            producer.sendConfigDeployedMessage(ModuleRequestMapper.toResetSettingEventRequest(deletedSetting));
            sendAuditMessage(AuditOperationEnum.DELETE, deletedSetting, username);
            return deletedSetting;
        }
        catch (ModelMapperException | MessageException | ConfigModelException ex) {
            throw new ServiceException("Error when deleting setting: " + settingKey + " moduleName: " + moduleName + " username: " + username,ex);
        }
    }
    
    @Override
    public List<SettingType> getList(String moduleName) throws ServiceException {
        try {
            return configModel.getList(moduleName);
        }
        catch (ConfigModelException e) {
            throw new ServiceException("Error when getting settings list for module: " + moduleName, e);
        }
    }

    @Override
    public Map<String, List<SettingType>> getCatalog() throws ServiceException {
        try {
            List<SettingsCatalogEntry> catalog = configModel.getSettingsCatalog();
            return getCatalogFromSettingCatalogResponse(catalog);
        }
        catch (ConfigModelException e) {
            throw new ServiceException(" Error when getting settings catalog",e);
        }
    }

    @Override
    public Map<String, Object> getAllModuleVersions() {
        return deployedModuleCatalog.getDeployedModuleVersions();
    }

    @Override
    public void updateModuleCatalog(String moduleName, String moduleVersion) {
        deployedModuleCatalog.setModuleVersion(moduleName, moduleVersion);
    }

    @Override
    public Map<String, String> getModuleVersion(String moduleName) {
        Map<String, String> moduleInfo = new HashMap<>();
        moduleInfo.put(moduleName, deployedModuleCatalog.getDeployedModuleVersion(moduleName));
        return moduleInfo;
    }

    private Map<String, List<SettingType>> getCatalogFromSettingCatalogResponse(List<SettingsCatalogEntry> catalog) {
        Map<String, List<SettingType>> settingsByModule = new HashMap<>();
        for (SettingsCatalogEntry entry : catalog) {
            settingsByModule.put(entry.getModuleName(), entry.getSettings());
        }

        return settingsByModule;
    }

    @Override
    public void setModuleTimestamp(String moduleName, Date timestamp) {
        ModuleAvailabilityBean.setTimestamp(moduleName, timestamp);
    }

    @Override
    public Map<String, Date> getModuleTimestamps() {
        return ModuleAvailabilityBean.getTimestamps();
    }

    @Override
    public List<SettingType> getGlobalSettings() throws ServiceException {
        try {
            return configModel.getGlobalSettings();
        } catch (ConfigModelException e) {
            throw new ServiceException("Error when getting global settings.",e);
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
            producer.sendAuditMessage(message);
        }
        catch (AuditModelMarshallException | MessageException e) {
            LOG.error("[ Error when sending message to Audit for operation: " + operation + " setting: " + setting + " userName:" + userName, e);
        }
    }

    @Override
    public List<SettingType> matchUnmatchedSettingsWithModule(String moduleName) throws ServiceException {
        try {
            return configModel.matchUnmatchedSettingsWithModule(moduleName);
        } catch (ConfigModelException e) {
            throw new ServiceException("Error when getting unmatched settings.",e);
        }
    }
}