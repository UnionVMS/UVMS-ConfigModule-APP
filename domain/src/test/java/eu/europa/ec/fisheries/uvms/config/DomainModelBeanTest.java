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

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import eu.europa.ec.fisheries.uvms.config.bean.ConfigDomainModelBean;
import eu.europa.ec.fisheries.uvms.config.dao.exception.DaoException;
import eu.europa.ec.fisheries.uvms.config.dao.exception.DaoMappingException;
import eu.europa.ec.fisheries.uvms.config.entity.component.Module;
import eu.europa.ec.fisheries.uvms.config.entity.component.Setting;
import eu.europa.ec.fisheries.uvms.config.model.exception.ConfigModelException;
import eu.europa.ec.fisheries.schema.config.types.v1.SettingType;


@RunWith(MockitoJUnitRunner.class)
public class DomainModelBeanTest {

    @Mock
    ConfigDao dao;

    @Mock
    ConfigMapper mapper;

    @InjectMocks
    private ConfigDomainModelBean model;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testCreateModel() throws ConfigModelException, DaoException, DaoMappingException {
        Long id = 1L;
        String username ="testUser";

        SettingType vessel = MockData.getModel(id.intValue());

        Setting entity = new Setting();
        entity.setId(id);

        Module module = new Module();
        module.setSettings(new ArrayList<Setting>());

        when(mapper.toEntity(vessel, username)).thenReturn(entity);
        when(dao.createSetting(any(Setting.class))).thenReturn(entity);
        when(mapper.toModel(any(Setting.class))).thenReturn(vessel);
        when(dao.getModuleByName("apa")).thenReturn(module);

        SettingType result = model.create(vessel, "apa", username);
        assertEquals(id, result.getId());
    }

    @Test
    public void testGetModelById() throws DaoException, ConfigModelException, DaoMappingException {
        Long id = 1L;
        Setting entity = new Setting();
        entity.setId(id);

        SettingType dto = new SettingType();
        dto.setId(id);

        when(mapper.toModel(any(Setting.class))).thenReturn(dto);
        when(dao.getSettingById(id)).thenReturn(entity);

        SettingType result = model.get(id);
        assertEquals(result.getId(), id);
    }
}