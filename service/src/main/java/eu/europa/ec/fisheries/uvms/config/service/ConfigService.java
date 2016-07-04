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
package eu.europa.ec.fisheries.uvms.config.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.ejb.Local;

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
    public SettingType create(SettingType setting, String moduleName, String username) throws ServiceException;

    /**
     * Create many settings.
     * 
     * @param settings list of settings to create
     * @throws ServiceException if unsucessful
     */
    public List<SettingType> createAll(List<SettingType> settings, String moduleName, String username) throws ServiceException;

    /**
     * 
     * @param id an ID
     * @return the setting with the given ID
     * @throws ServiceException if unsuccessful
     */
    public SettingType getById(Long id) throws ServiceException;

    /**
     * Updates a settig.
     *
     * @param id the ID of the setting
     * @param setting an updated setting
     * @return the updated setting
     * @throws ServiceException if unsuccessful
     */
    public SettingType update(Long id, SettingType setting, String username) throws ServiceException;

    /**
     * Deletes a setting.
     * 
     * @param id ID of the setting to be deleted
     * @return the deleted setting
     * @throws ServiceException if unsuccessful
     */
    public SettingType delete(Long id, String username) throws ServiceException;

    /**
     * 
     * @param settingKey key of the setting to be deleted
     * @param moduleName name of the setting's module
     * @return the deleted setting
     * @throws ServiceException if unsuccessful
     */
    public SettingType delete(String settingKey, String moduleName, String username) throws ServiceException;

    /**
     * Get a list of settings.
     * If the module is missing in the database, this method will return null (not an empty list).
     *
     * @param moduleName name of a module
     * @return list of settings for the module, or null if module is missing
     * @throws ServiceException if unsuccessful
     */
    public List<SettingType> getList(String moduleName) throws ServiceException;

    /**
     * Get a catlog of setting for all modules.

     * @return a mapping from module name to list of settings
     * @throws ServiceException if unsuccessful
     */
    public Map<String, List<SettingType>> getCatalog() throws ServiceException;

    /**
     * Sets a new timestamp for the module. 
     *  
     * @param moduleName a module name
     * @param timestamp a timestamp
     */
    public void setModuleTimestamp(String moduleName, Date timestamp);

    /**
     * @return map of timestamps by module name
     */
    public Map<String, Date> getModuleTimestamps();

    /**
     * @return
     * @throws ServiceException
     */
    public List<SettingType> getGlobalSettings() throws ServiceException;
    
}