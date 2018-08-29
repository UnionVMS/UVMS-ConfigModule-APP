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
package eu.europa.ec.fisheries.uvms.config.mapper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ejb.Stateless;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.europa.ec.fisheries.schema.config.types.v1.SettingType;
import eu.europa.ec.fisheries.uvms.config.dao.exception.DaoMappingException;
import eu.europa.ec.fisheries.uvms.config.entity.component.Setting;

@Stateless
public class ConfigMapperBean implements ConfigMapper {

    private final static Logger LOG = LoggerFactory.getLogger(ConfigMapperBean.class);

    private Setting mapToEntity(Setting entity, SettingType setting, String username) throws DaoMappingException {
        try {
            entity.setKey(setting.getKey());
            entity.setValue(setting.getValue());
            entity.setDescription(setting.getDescription());
            entity.setLastModified(new Date());
            entity.setUpdatedBy(username);
            return entity;
        } catch (Exception e) {
            LOG.error("[ Error when mapping to entity. ] {}", e.getMessage());
            throw new DaoMappingException("[ Error when mapping to entity. ]", e);
        }
    }

    SettingType mapToModel(SettingType model, Setting entity) throws DaoMappingException {
        try {
            if (model == null) {
                model = new SettingType();
            }

            model.setId(entity.getId());
            model.setKey(entity.getKey());
            model.setValue(entity.getValue());
            model.setGlobal(entity.isGlobal());
            model.setDescription(entity.getDescription());

            if (entity.getModule() != null) {
                model.setModule(entity.getModule().getModuleName());
            }

            return model;
        } catch (Exception e) {
            LOG.error("[ Error when mapping to model. ] {}", e.getMessage());
            throw new DaoMappingException("[ Error when mapping to model. ]", e);
        }
    }

    @Override
    public Setting toEntity(SettingType vessel, String username) throws DaoMappingException {
        Setting entity = new Setting();
        entity.setGlobal(vessel.isGlobal()); // Set globality once and never change it.
        return mapToEntity(entity, vessel, username);
    }

    @Override
    public Setting toEntity(Setting entity, SettingType vessel, String username) throws DaoMappingException {
        return mapToEntity(entity, vessel, username);
    }

    @Override
    public SettingType toModel(Setting vesselEntity) throws DaoMappingException {
        return mapToModel(null, vesselEntity);
    }

    @Override
    public List<SettingType> toModel(List<Setting> entities) throws DaoMappingException {
        List<SettingType> models = new ArrayList<>();
        for (Setting entity : entities) {
            models.add(toModel(entity));
        }

        return models;
    }
}
