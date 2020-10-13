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
package eu.europa.ec.fisheries.uvms.config.service.dao.bean;

import eu.europa.ec.fisheries.uvms.config.service.constants.UvmsConstants;
import eu.europa.ec.fisheries.uvms.config.service.entity.component.Module;
import eu.europa.ec.fisheries.uvms.config.service.entity.component.Setting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.List;

@Stateless
public class ConfigDaoBean {

    final static Logger LOG = LoggerFactory.getLogger(ConfigDaoBean.class);

    @PersistenceContext
    protected EntityManager em;


    public Setting createSetting(Setting entity) {
        em.persist(entity);
        return entity;
    }

    public Setting getSettingById(Long id) {
        return em.find(Setting.class, id);
    }

    public Setting updateSetting(Setting entity) {
        em.merge(entity);
        em.flush();
        return entity;
    }

    public Setting deleteSetting(Long id) {
        Setting setting = em.find(Setting.class, id);
        em.remove(setting);
        return setting;
    }

    public List<Setting> getGlobalSettings() {
        TypedQuery<Setting> query = em.createNamedQuery(UvmsConstants.SETTING_FIND_GLOBALS, Setting.class);
        return query.getResultList();
    }

    public Setting getGlobalSetting(String settingKey) {
        TypedQuery<Setting> query = em.createNamedQuery(UvmsConstants.SETTING_FIND_GLOBAL_BY_KEY, Setting.class);
        query.setParameter("key", settingKey);
        List<Setting> settings = query.getResultList();
        if (settings.isEmpty()) {
            return null;
        }

        return settings.get(0);
    }

    public Setting getSettingByKeyAndModule(String settingKey, String moduleName) {
        TypedQuery<Setting> query = em.createNamedQuery(UvmsConstants.SETTING_FIND_BY_KEY_AND_MODULE, Setting.class);
        query.setParameter("key", settingKey);
        query.setParameter("moduleName", moduleName);
        List<Setting> settings = query.getResultList();
        if (settings.isEmpty()) {
            return null;
        }

        return settings.get(0);
    }

    public Module getModuleByName(String moduleName) {
        TypedQuery<Module> query = em.createNamedQuery(UvmsConstants.MODULE_FIND_BY_NAME, Module.class);
        query.setParameter("moduleName", moduleName);
        List<Module> modules = query.getResultList();
        return modules.isEmpty() ? null : modules.get(0);
    }

    public Module createModule(String moduleName){
        Module module = new Module();
        module.setModuleName(moduleName);
        em.persist(module);
        return module;
    }

    public List<Module> getModules() {
        TypedQuery<Module> query = em.createNamedQuery(UvmsConstants.MODULE_LIST_ALL, Module.class);
        return query.getResultList();
    }
}