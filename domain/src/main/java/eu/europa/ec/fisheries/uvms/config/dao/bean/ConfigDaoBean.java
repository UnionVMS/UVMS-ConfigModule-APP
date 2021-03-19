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
package eu.europa.ec.fisheries.uvms.config.dao.bean;

import javax.ejb.Stateless;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.util.List;

import eu.europa.ec.fisheries.uvms.config.constant.UvmsConstants;
import eu.europa.ec.fisheries.uvms.config.dao.ConfigDao;
import eu.europa.ec.fisheries.uvms.config.dao.Dao;
import eu.europa.ec.fisheries.uvms.config.dao.exception.DaoException;
import eu.europa.ec.fisheries.uvms.config.dao.exception.NoEntityFoundException;
import eu.europa.ec.fisheries.uvms.config.entity.component.Module;
import eu.europa.ec.fisheries.uvms.config.entity.component.Setting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Stateless
public class ConfigDaoBean extends Dao implements ConfigDao {

    final static Logger LOG = LoggerFactory.getLogger(ConfigDaoBean.class);

    @Override
    public Setting createSetting(Setting entity) throws DaoException {
        try {
            em.persist(entity);
            return entity;
        } catch (Exception e) {
            LOG.error("[ Error when creating setting. ] {}", e.getMessage());
            throw new DaoException("[ Error when creating setting. ]", e);
        }
    }

    @Override
    public Setting getSettingById(Long id) throws DaoException {
        try {
            return em.find(Setting.class, id);
        } catch (Exception e) {
            LOG.error("[ Error when getting setting by ID. ] {}", e.getMessage());
            throw new DaoException("[ Error when getting entity by ID. ] ", e);
        }
    }

    @Override
    public Setting updateSetting(Setting entity) throws DaoException {
        try {
            em.merge(entity);
            em.flush();
            return entity;
        } catch (Exception e) {
            LOG.error("[ Error when updating entity. ] {}", e.getMessage());
            throw new DaoException("[ Error when updating entity. ]", e);
        }
    }

    @Override
    public Setting deleteSetting(Long id) throws DaoException {
        try {
            Setting setting = em.find(Setting.class, id);
            em.remove(setting);
            return setting;
        } catch (NoResultException e) {
            LOG.error("[ Error when deleting, could not find entity by ID. ] {}", e);
            throw new NoEntityFoundException("[ Error when getting entity. by ID. ]", e);
        }
    }

    @Override
    public List<Setting> getGlobalSettings() throws DaoException {
        try {
            TypedQuery<Setting> query = em.createNamedQuery(UvmsConstants.SETTING_FIND_GLOBALS, Setting.class);
            return query.getResultList();
        } catch (Exception e) {
            LOG.error("[ Error when getting global settings list. ] {}", e.getMessage());
            throw new DaoException("[ Error when getting global settings list ] ", e);
        }
    }

    @Override
    public Setting getGlobalSetting(String settingKey) throws DaoException {
        try {
            TypedQuery<Setting> query = em.createNamedQuery(UvmsConstants.SETTING_FIND_GLOBAL_BY_KEY, Setting.class);
            query.setParameter("key", settingKey);
            List<Setting> settings = query.getResultList();
            if (settings.isEmpty()) {
                return null;
            }

            return settings.get(0);
        } catch (Exception e) {
            LOG.error("[ Error when getting global setting. ] {}", e.getMessage());
            throw new DaoException("[ Error when getting global setting. ] ", e);
        }
    }

    @Override
    public Setting getSetting(String settingKey, String moduleName) throws DaoException {
        try {
            TypedQuery<Setting> query = em.createNamedQuery(UvmsConstants.SETTING_FIND_BY_KEY_AND_MODULE, Setting.class);
            query.setParameter("key", settingKey);
            query.setParameter("moduleName", moduleName);
            List<Setting> settings = query.getResultList();
            if (settings.isEmpty()) {
                return null;
            }

            return settings.get(0);
        } catch (Exception e) {
            LOG.error("[ Error when getting module setting. ] {}", e.getMessage());
            throw new DaoException("[ Error when getting module setting. ] ", e);
        }
    }

    @Override
    public Module getModuleByName(String moduleName) throws DaoException {
        try {
            TypedQuery<Module> query = em.createNamedQuery(UvmsConstants.MODULE_FIND_BY_NAME, Module.class);
            query.setParameter("moduleName", moduleName);
            List<Module> modules = query.getResultList();
            return modules.isEmpty() ? null : modules.get(0);
        } catch (Exception e) {
            LOG.error("[ Error when getting module by name. ] {}", e.getMessage());
            throw new DaoException("[ Error when getting module by name. ]", e);
        }
    }

    @Override
    public Module createModule(String moduleName) throws DaoException {
        try {
            Module module = new Module();
            module.setModuleName(moduleName);
            em.persist(module);
            return module;
        } catch (Exception e) {
            LOG.error("[ Error when creating module. ] {}", e.getMessage());
            throw new DaoException("[ Error when creating module. ]", e);
        }
    }

    @Override
    public List<Setting> updateSettingsMissingModuleId(String moduleName) throws DaoException {
        try{
            Module module = getModuleByName(moduleName);

            TypedQuery<Setting> query = em.createNamedQuery(UvmsConstants.SETTING_FIND_BY_NAME_CONTAINING_MODULE, Setting.class);
            query.setParameter("moduleName", module.getModuleName());
            List<Setting> settings = query.getResultList();
            for(Setting setting: settings) {
                if(setting.getModule() == null) {
                    setting.setModule(module);
                }
                updateSetting(setting);
            }
            return settings;
        } catch (Exception e) {
            LOG.error("[ Error when matching setting with module. ] {}", e.getMessage());
            throw new DaoException(e.getMessage());
        }
    }

    @Override
    public List<Module> getModules() throws DaoException {
        try {
            TypedQuery<Module> query = em.createNamedQuery(UvmsConstants.MODULE_LIST_ALL, Module.class);
            return query.getResultList();
        } catch (Exception e) {
            LOG.error("[ Error when getting module names. ] {}", e.getMessage());
            throw new DaoException("[ Error when getting module names. ]", e);
        }
    }
}