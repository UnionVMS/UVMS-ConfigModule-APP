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
package eu.europa.ec.fisheries.uvms.config.dao;

import java.util.List;

import javax.ejb.Local;

import eu.europa.ec.fisheries.uvms.config.dao.exception.DaoException;
import eu.europa.ec.fisheries.uvms.config.entity.component.Module;
import eu.europa.ec.fisheries.uvms.config.entity.component.Setting;

@Local
public interface ConfigDao {

    /**
     * Create a setting in the database.
     *
     * @param setting a setting
     * @return the created setting
     * @throws DaoException if the setting could not be created
     */
    public Setting createSetting(Setting setting) throws DaoException;

    /**
     * Get a setting by its entity ID.
     *
     * @param id the ID of the setting
     * @return the setting with the given ID
     * @throws DaoException if the setting could not be returned
     */
    public Setting getSettingById(Long id) throws DaoException;

    /**
     * Updates a setting in the database.
     *
     * @param vessel a setting
     * @return the updated setting
     * @throws DaoException if the setting could not be updated
     */
    public Setting updateSetting(Setting vessel) throws DaoException;

    /**
     * Delete a setting from the database.
     *
     * @param settingId entity ID of a setting
     * @throws DaoException if the setting could not be deleted
     */
    public Setting deleteSetting(Long settingId) throws DaoException;

    /**
     * @return list of all global settings
     * @throws DaoException if unsuccessful
     */
    public List<Setting> getGlobalSettings() throws DaoException;

    /**
     * @param settingKey key of the setting
     * @return global setting with key
     * @throws DaoException if unsuccessful
     */
    public Setting getGlobalSetting(String settingKey) throws DaoException;

    /**
     * @param settingKey key of the setting
     * @param moduleName name of the module
     * @return setting with key
     * @throws DaoException if unsuccessful
     */
    public Setting getSetting(String settingKey, String moduleName) throws DaoException;

    /**
     * @return list of all modules
     * @throws DaoException if unsuccessful
     */
    public List<Module> getModules() throws DaoException; 

    /**
     * @param moduleName name of a module
     * @return the module with that name
     * @throws DaoException if unsuccessful
     */
    public Module getModuleByName(String moduleName) throws DaoException;

    /**
     * @param moduleName a name
     * @return the created module
     * @throws DaoException if unsuccessful
     */
    public Module createModule(String moduleName) throws DaoException;

}
