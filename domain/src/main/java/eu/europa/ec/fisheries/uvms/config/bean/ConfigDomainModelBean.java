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
package eu.europa.ec.fisheries.uvms.config.bean;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.europa.ec.fisheries.schema.config.types.v1.SettingType;
import eu.europa.ec.fisheries.schema.config.types.v1.SettingsCatalogEntry;
import eu.europa.ec.fisheries.uvms.config.ConfigDomainModel;
import eu.europa.ec.fisheries.uvms.config.dao.ConfigDao;
import eu.europa.ec.fisheries.uvms.config.dao.exception.DaoException;
import eu.europa.ec.fisheries.uvms.config.dao.exception.DaoMappingException;
import eu.europa.ec.fisheries.uvms.config.dao.exception.InputArgumentException;
import eu.europa.ec.fisheries.uvms.config.entity.component.Module;
import eu.europa.ec.fisheries.uvms.config.entity.component.Setting;
import eu.europa.ec.fisheries.uvms.config.mapper.ConfigMapper;
import eu.europa.ec.fisheries.uvms.config.model.exception.ConfigModelException;

@Stateless
public class ConfigDomainModelBean implements ConfigDomainModel {

    final static Logger LOG = LoggerFactory.getLogger(ConfigDomainModelBean.class);

    @EJB
    ConfigDao dao;

    @EJB
    ConfigMapper mapper;

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public SettingType create(SettingType setting, String moduleName, String username) throws ConfigModelException {
        try {
            return createSetting(setting, getModule(moduleName), username);
        }
        catch (DaoException | DaoMappingException e) {
            LOG.error("[ Error when creating setting. {}] {}",setting, e.getMessage());
            throw new ConfigModelException("Error when creating setting.", e);
        }
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public List<SettingType> createAll(List<SettingType> settings, String moduleName, String username) throws ConfigModelException {
        Module module = getModule(moduleName);
        List<SettingType> createdSettings = new ArrayList<>();
        for (SettingType setting : settings) {
            try {
                createdSettings.add(createSetting(setting, module, username));
            }
            catch (DaoException | DaoMappingException e) {
                // If single setting could not be created, log error and skip to next setting.
                LOG.error("[ Error when creating single setting: {} ] {}",settings, e.getMessage());
            }
        }

        return createdSettings;
    }

    @Override
    public SettingType get(Long settingId) throws ConfigModelException {
        if (settingId == null) {
            LOG.error("[ ID is null, returning Exception. ]");
            throw new InputArgumentException("ID is null", null);
        }

        try {
            return mapper.toModel(dao.getSettingById(settingId));
        }
        catch (DaoException | DaoMappingException e) {
            LOG.error("[ Error when updating setting: {} ] {}",settingId, e.getMessage());
            throw new ConfigModelException("[ Error when updating setting. ]", e);
        }
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public SettingType update(SettingType setting, String username) throws ConfigModelException, InputArgumentException {
        if (setting == null) {
            LOG.error("[ Model is null, returning Exception ]");
            throw new InputArgumentException("Model is null", null);
        }

        if (setting.getId() == null) {
            LOG.error("[ ID of the model is null, returning Exception. ]");
            throw new InputArgumentException("ID of the model is null", null);
        }

        try {
            Setting entity = dao.getSettingById(setting.getId());
            entity = mapper.toEntity(entity, setting, username);
            Setting updatedEntity = dao.updateSetting(entity);
            return mapper.toModel(updatedEntity);
        }
        catch (DaoException | DaoMappingException e) {
            LOG.error("[ Error when updating setting: {} ] {}",setting, e.getMessage());
            throw new ConfigModelException("[ Error when updating setting. ]", e);
        }
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public SettingType delete(Long settingId) throws ConfigModelException, InputArgumentException {
        try {
            if (settingId == null) {
                LOG.error("[ ID is null, returning Exception. ]");
                throw new InputArgumentException("ID is null", null);
            }

            return mapper.toModel(dao.deleteSetting(settingId));
        }
        catch (DaoException | DaoMappingException e) {
            LOG.error("[ Error when deleting setting {} ] {}",settingId, e.getMessage());
            throw new ConfigModelException("[ Error when deleting setting. ]", e);
        }
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public SettingType delete(String settingKey, String moduleName) throws ConfigModelException {
        try {
            Setting setting = null;
            if (moduleName == null) {
                setting = dao.getGlobalSetting(settingKey);
            }
            else {
                setting = dao.getSetting(settingKey, moduleName);
            }

            if (setting == null) {
                throw new ConfigModelException("Could not find setting " + settingKey + " in module " + moduleName + ".");
            }

            Setting deletedSetting = dao.deleteSetting(setting.getId());
            return mapper.toModel(deletedSetting);
        }
        catch (DaoException | DaoMappingException e) {
            LOG.error("[ Error when deleting setting. settingKey: {} moduleName: {} ] {}",settingKey,moduleName, e.getMessage());
            throw new ConfigModelException("[ Error when deleting setting. ]");
        }
    }

    @Override
    public List<SettingType> getList(String moduleName) throws ConfigModelException, InputArgumentException {
        if (moduleName == null) {
            LOG.error("[ No module name when getting list. ]");
            throw new InputArgumentException("No module name.");
        }

        try {
            Module module = dao.getModuleByName(moduleName);
            if (module == null) {
                return null;
            }
            List<Setting> matchedSettings = dao.updateSettingsMissingModuleId(module.getModuleName());
            ArrayList<Setting> settings = new ArrayList<>();
            settings.addAll(module.getSettings());
            settings.addAll(dao.getGlobalSettings());
            settings.addAll(matchedSettings);
            return mapper.toModel(settings);
        }
        catch (DaoException | DaoMappingException e) {
            LOG.error("[ Error when getting settings list:{} ] {}",moduleName, e);
            throw new ConfigModelException("[ Error when getting settings list. ]", e);
        }
    }

    @Override
    public List<SettingsCatalogEntry> getSettingsCatalog() throws ConfigModelException {
        try {
            List<SettingsCatalogEntry> catalog = new ArrayList<>();
            List<SettingType> globalSettings = mapper.toModel(dao.getGlobalSettings());
            for (Module module : dao.getModules()) {
                SettingsCatalogEntry entry = new SettingsCatalogEntry();
                entry.setModuleName(module.getModuleName());
                entry.getSettings().addAll(mapper.toModel(module.getSettings()));
                entry.getSettings().addAll(globalSettings);
                catalog.add(entry);
            }

            return catalog;
        }
        catch (DaoException | DaoMappingException e) {
            throw new ConfigModelException("[ Error when listing settings catalog. ]", e);
        }
    }

    private static void validateSetting(SettingType setting, Module module) throws InputArgumentException {
        if (setting == null) {
            LOG.error("[ Setting is null, returning Exception ]");
            throw new InputArgumentException("Setting is null", null);
        }

        if (!setting.isGlobal() && module == null) {
            LOG.error("[ Non-global setting has no module. ]");
            throw new InputArgumentException("Non-global setting has no module.");
        }
    }

    private SettingType createSetting(SettingType setting, Module module, String username) throws InputArgumentException, DaoMappingException, DaoException {
        validateSetting(setting, module);

        if (module == null) {
            return createGlobalSetting(setting, username);
        }
        else {
            return createModuleSetting(module, setting, username);
        }
    }

    private Module getModule(String moduleName) throws ConfigModelException {
        if (moduleName == null) {
            return null;
        }

        try {
            Module module = dao.getModuleByName(moduleName);
            if (module == null) {
                module = dao.createModule(moduleName);
            }
            return module;
        }
        catch (DaoException e) {
            LOG.error("[ Error when getting or creating module:{} ] {}",moduleName, e.getMessage());
            throw new ConfigModelException("[ Could not create settings for module " + moduleName + ". ]");
        }
    }

    /**
     * Creates a global setting.
     */
    private SettingType createGlobalSetting(SettingType setting, String username) throws DaoMappingException, DaoException {
        Setting existingSetting = dao.getGlobalSetting(setting.getKey());
        if (existingSetting != null) {
            // Update existing global setting
            Setting updatedSetting = mapper.toEntity(existingSetting, setting, username);
            return mapper.toModel(updatedSetting);
        }

        // With no module available, setting must be global.
        setting.setGlobal(true);

        // Create new global setting
        Setting entity = mapper.toEntity(setting, username);
        Setting createdSetting = dao.createSetting(entity);
        return mapper.toModel(createdSetting);
    }

    /**
     * Creates a setting, associated with a specific module. 
     */
    private SettingType createModuleSetting(Module module, SettingType setting, String username) throws DaoMappingException, DaoException {
        Setting existingSetting = dao.getSetting(setting.getKey(), module.getModuleName());
        if (existingSetting != null) {
            // Update existing setting
            Setting updatedSetting = mapper.toEntity(existingSetting, setting, username);
            return mapper.toModel(updatedSetting);
        }

        // Create new setting
        Setting entity = mapper.toEntity(setting, username);
        Setting createdSetting = dao.createSetting(entity);
        createdSetting.setModule(module);
        module.getSettings().add(createdSetting);
        return mapper.toModel(createdSetting);
    }

    @Override
    public List<SettingType> getGlobalSettings() throws ConfigModelException {
        try {
            List<Setting> globalSettings = dao.getGlobalSettings();
            return mapper.toModel(globalSettings);
        } catch (DaoException | DaoMappingException e) {
            LOG.error("[ Error when getting global settings. ] {}", e.getMessage());
            throw new ConfigModelException("[ Error when getting global settings. ]");
        }
    }

    @Override
    public List<SettingType> matchUnmatchedSettingsWithModule(String moduleName) throws ConfigModelException {
        try {
            List<Setting> matchedSettings = dao.updateSettingsMissingModuleId(moduleName);
            return mapper.toModel(matchedSettings);
        } catch (DaoException | DaoMappingException e) {
            LOG.error("[ Error when matching unmatched settings. ] {}", e.getMessage());
            throw new ConfigModelException("[ Error when matching unmatched settings. ]");
        }
    }
}