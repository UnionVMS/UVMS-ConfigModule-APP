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
package eu.europa.ec.fisheries.uvms.config.service;

import javax.ejb.Local;
import java.util.Date;
import java.util.List;
import java.util.Map;

import eu.europa.ec.fisheries.schema.config.types.v1.SettingType;
import eu.europa.ec.fisheries.uvms.config.service.exception.ServiceException;

@Local
public interface ConfigService {

    /**
     * Create setting.
     *
     * @param setting a setting
     * @param moduleName name of the module
     * @return the created setting
     * @throws ServiceException if unsuccessful
     */
    SettingType create(SettingType setting, String moduleName, String username) throws ServiceException;

    /**
     * Create many settings.
     * 
     * @param settings list of settings to create
     * @throws ServiceException if unsucessful
     */
    List<SettingType> createAll(List<SettingType> settings, String moduleName, String username) throws ServiceException;

    /**
     * 
     * @param id an ID
     * @return the setting with the given ID
     * @throws ServiceException if unsuccessful
     */
    SettingType getById(Long id) throws ServiceException;

    /**
     * Updates a settig.
     *
     * @param id the ID of the setting
     * @param setting an updated setting
     * @return the updated setting
     * @throws ServiceException if unsuccessful
     */
    SettingType update(Long id, SettingType setting, String username) throws ServiceException;

    /**
     * Deletes a setting.
     * 
     * @param id ID of the setting to be deleted
     * @return the deleted setting
     * @throws ServiceException if unsuccessful
     */
    SettingType delete(Long id, String username) throws ServiceException;

    /**
     * 
     * @param settingKey key of the setting to be deleted
     * @param moduleName name of the setting's module
     * @return the deleted setting
     * @throws ServiceException if unsuccessful
     */
    SettingType delete(String settingKey, String moduleName, String username) throws ServiceException;

    /**
     * Get a list of settings.
     * If the module is missing in the database, this method will return null (not an empty list).
     *
     * @param moduleName name of a module
     * @return list of settings for the module, or null if module is missing
     * @throws ServiceException if unsuccessful
     */
    List<SettingType> getList(String moduleName) throws ServiceException;

    /**
     * Get a catlog of setting for all modules.

     * @return a mapping from module name to list of settings
     * @throws ServiceException if unsuccessful
     */
    Map<String, List<SettingType>> getCatalog() throws ServiceException;

    /**
     * Get a catalog of all deployed modules.

     * @return a mapping from module name to deployed version
     * @throws ServiceException if unsuccessful
     */
    Map<String, Object> getAllModuleVersions() throws ServiceException;

    /**
     * Updates catalog with the given module and its version.
     * @param moduleName the name of the module
     * @param moduleVersion the version of the module
     */
    void updateModuleCatalog(String moduleName, String moduleVersion);

    /**
     * Get a modules version.
     * @param moduleName the name of the module
     * @return the version of the module
     */
    Map<String, String> getModuleVersion(String moduleName);

    /**
     * Sets a new timestamp for the module.
     *
     * @param moduleName a module name
     * @param timestamp a timestamp
     */
    void setModuleTimestamp(String moduleName, Date timestamp);

    /**
     * @return map of timestamps by module name
     */
    Map<String, Date> getModuleTimestamps();

    /**
     * @return
     * @throws ServiceException
     */
    List<SettingType> getGlobalSettings() throws ServiceException;


    /**
     * Match unmatched settings with modules 
     *
     */
    List<SettingType> matchUnmatchedSettingsWithModule(String moduleName) throws ServiceException;
}