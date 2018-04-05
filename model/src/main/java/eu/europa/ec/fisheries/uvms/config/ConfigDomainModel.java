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
package eu.europa.ec.fisheries.uvms.config;

import java.util.List;

import javax.ejb.Local;

import eu.europa.ec.fisheries.schema.config.types.v1.SettingType;
import eu.europa.ec.fisheries.schema.config.types.v1.SettingsCatalogEntry;
import eu.europa.ec.fisheries.uvms.config.model.exception.ConfigModelException;

@Local
public interface ConfigDomainModel {

    /**
     * Create a new setting.
     *
     * If a setting with the same key exists, updates the existing setting.
     *
     * @param setting a setting
     * @param moduleName a module name
     * @return the created or updated setting
     * @throws ConfigModelException if unsuccessful
     */
    SettingType create(SettingType setting, String moduleName, String username) throws ConfigModelException;

    /**
     * Create many settings at once.
     *
     * If any setting already exists by key, they will be updated.
     *
     * @param settings list of settings
     * @param moduleName module name
     * @return list of created or updated settings
     * @throws ConfigModelException if unsuccessful
     */
    List<SettingType> createAll(List<SettingType> settings, String moduleName, String username) throws ConfigModelException;

    /**
     * Get an existing setting.
     *
     * @param id the ID of a setting
     * @return the setting
     * @throws ConfigModelException if unsuccessful
     */
    SettingType get(Long id) throws ConfigModelException;

    /**
     * Updates an existing setting.
     *
     * @param model modified setting
     * @param username username
     * @return the updated setting
     * @throws ConfigModelException if unsuccessful
     */
    SettingType update(SettingType model, String username) throws ConfigModelException;

    /**
     * Delete an existing setting.
     *
     * @param settingId the ID of a setting
     * @throws ConfigModelException if unsuccessful
     */
    SettingType delete(Long settingId) throws ConfigModelException;

    /**
     * Deletes as setting by key, and module.
     *
     * @param settingKey key of the setting
     * @param moduleName name of the module, or null for global settings
     * @throws ConfigModelException if unsuccessful
     */
    SettingType delete(String settingKey, String moduleName) throws ConfigModelException;

    /**
     * Lists all settings for a module, including global settings.
     *
     * @param moduleName the name of a module
     * @return list of settings for that module, including global settings
     * @throws ConfigModelException if unsuccessful
     */
    List<SettingType> getList(String moduleName) throws ConfigModelException;

    /**
     * List of all module settings. 
     *
     * @return a list of setting catalog entries
     * @throws ConfigModelException if unsuccessful
     */
    List<SettingsCatalogEntry> getSettingsCatalog() throws ConfigModelException;

    /**
     * @return global settings
     */
    List<SettingType> getGlobalSettings() throws ConfigModelException;

}