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
import javax.inject.Inject;

import eu.europa.ec.fisheries.uvms.config.dao.bean.ConfigDaoBean;
import eu.europa.ec.fisheries.uvms.config.mapper.ConfigMapperBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.europa.ec.fisheries.schema.config.types.v1.SettingType;
import eu.europa.ec.fisheries.schema.config.types.v1.SettingsCatalogEntry;
import eu.europa.ec.fisheries.uvms.config.entity.component.Module;
import eu.europa.ec.fisheries.uvms.config.entity.component.Setting;

@Stateless
public class ConfigDomainModelBean {

    final static Logger LOG = LoggerFactory.getLogger(ConfigDomainModelBean.class);

    @Inject
    ConfigDaoBean dao;

    @Inject
    ConfigMapperBean mapper;

    public SettingType create(SettingType setting, String moduleName, String username) {
            return createSetting(setting, getModule(moduleName), username);
    }

    public List<SettingType> createAll(List<SettingType> settings, String moduleName, String username) {
        Module module = getModule(moduleName);
        List<SettingType> createdSettings = new ArrayList<>();
        for (SettingType setting : settings) {
            try {
                createdSettings.add(createSetting(setting, module, username));
            }
            catch (Exception e) {
                // If single setting could not be created, log error and skip to next setting.
                LOG.error("[ Error when creating single setting: {} ] {}",settings, e.getMessage());
            }
        }

        return createdSettings;
    }

    public SettingType get(Long settingId) {
        if (settingId == null) {
            LOG.error("[ ID is null, returning Exception. ]");
            throw new IllegalArgumentException("ID is null");
        }
        return mapper.toModel(dao.getSettingById(settingId));
    }

    public SettingType update(SettingType setting, String username) {
        if (setting == null) {
            LOG.error("[ Model is null, returning Exception ]");
            throw new IllegalArgumentException("Model is null");
        }

        if (setting.getId() == null) {
            LOG.error("[ ID of the model is null, returning Exception. ]");
            throw new IllegalArgumentException("ID of the model is null");
        }

        Setting entity = dao.getSettingById(setting.getId());
        entity = mapper.toEntity(entity, setting, username);
        Setting updatedEntity = dao.updateSetting(entity);
        return mapper.toModel(updatedEntity);
    }

    public SettingType delete(Long settingId){
        if (settingId == null) {
            LOG.error("[ ID is null, returning Exception. ]");
            throw new IllegalArgumentException("ID is null");
        }

        return mapper.toModel(dao.deleteSetting(settingId));
    }

    public SettingType delete(String settingKey, String moduleName) {
        Setting setting = null;
        if (moduleName == null) {
            setting = dao.getGlobalSetting(settingKey);
        }
        else {
            setting = dao.getSetting(settingKey, moduleName);
        }

        if (setting == null) {
            throw new IllegalArgumentException("Could not find setting " + settingKey + " in module " + moduleName + ".");
        }

        Setting deletedSetting = dao.deleteSetting(setting.getId());
        return mapper.toModel(deletedSetting);
    }

    public List<SettingType> getList(String moduleName) {
        if (moduleName == null) {
            LOG.error("[ No module name when getting list. ]");
            throw new IllegalArgumentException("No module name.");
        }

        Module module = dao.getModuleByName(moduleName);
        if (module == null) {
            return null;
        }

        ArrayList<Setting> settings = new ArrayList<>();
        settings.addAll(module.getSettings());
        settings.addAll(dao.getGlobalSettings());
        return mapper.toModel(settings);
    }

    public List<SettingsCatalogEntry> getSettingsCatalog() {
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

    private static void validateSetting(SettingType setting, Module module) {
        if (setting == null) {
            LOG.error("[ Setting is null, returning Exception ]");
            throw new IllegalArgumentException("Setting is null", null);
        }

        if (!setting.isGlobal() && module == null) {
            LOG.error("[ Non-global setting has no module. ]");
            throw new IllegalArgumentException("Non-global setting has no module.");
        }
    }

    private SettingType createSetting(SettingType setting, Module module, String username) {
        validateSetting(setting, module);

        if (module == null) {
            return createGlobalSetting(setting, username);
        }
        else {
            return createModuleSetting(module, setting, username);
        }
    }

    private Module getModule(String moduleName) {
        if (moduleName == null) {
            return null;
        }

        Module module = dao.getModuleByName(moduleName);
        if (module == null) {
            module = dao.createModule(moduleName);
        }

        return module;
    }

    /**
     * Creates a global setting.
     */
    private SettingType createGlobalSetting(SettingType setting, String username) {
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
    private SettingType createModuleSetting(Module module, SettingType setting, String username) {
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

    public List<SettingType> getGlobalSettings() {
        List<Setting> globalSettings = dao.getGlobalSettings();
        return mapper.toModel(globalSettings);
    }

}