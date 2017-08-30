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

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import eu.europa.ec.fisheries.uvms.config.MockData;
import eu.europa.ec.fisheries.uvms.config.dao.exception.DaoException;
import eu.europa.ec.fisheries.uvms.config.dao.exception.DaoMappingException;
import eu.europa.ec.fisheries.uvms.config.entity.component.Setting;
import eu.europa.ec.fisheries.schema.config.types.v1.SettingType;

@RunWith(MockitoJUnitRunner.class)
public class MapperTest {


    @InjectMocks
    private ConfigMapperBean mapper;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testEntityToModel() throws DaoException, DaoMappingException {
        Long id = 1L;
        Setting entity = MockData.getEntity(id);

        SettingType result = mapper.toModel(entity);

        assertEquals(id, result.getId());
        assertSame(entity.getValue(), result.getValue());
    }

    @Test
    public void testModelToEntity() throws DaoException, DaoMappingException {
        Long id = 1L;
        String user = "testUser";
        SettingType model = MockData.getModel(id);

        Setting result = mapper.toEntity(model, user);

        assertSame(model.getValue(), result.getValue());
    }

    @Test
    public void testEntityAndModelToEntity() throws DaoException, DaoMappingException {
        Long id = 1L;
        String user = "testUser";
        Setting entity = MockData.getEntity(id);
        SettingType vessel = MockData.getModel(1L);

        Setting result = mapper.toEntity(entity, vessel, user);

        assertSame(result.getValue(), vessel.getValue());
    }

    @Test
    public void testEntityAndModelToModel() throws DaoException, DaoMappingException {
        Setting entity = MockData.getEntity(1L);
        SettingType result = mapper.mapToModel(null, entity);

        assertSame(result.getValue(), entity.getValue());
    }
}