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
package eu.europa.ec.fisheries.uvms.config.service.bean;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.mockito.Mockito;
import eu.europa.ec.fisheries.uvms.config.service.MockData;
import eu.europa.ec.fisheries.uvms.config.service.bean.ConfigDomainModelBean;
import eu.europa.ec.fisheries.uvms.config.service.dao.bean.ConfigDaoBean;
import eu.europa.ec.fisheries.uvms.config.service.entity.component.Module;
import eu.europa.ec.fisheries.uvms.config.service.entity.component.Setting;
import eu.europa.ec.fisheries.uvms.config.service.mapper.ConfigMapper;
import eu.europa.ec.fisheries.schema.config.types.v1.SettingType;
import eu.europa.ec.fisheries.schema.config.types.v1.SettingsCatalogEntry;

public class ConfigDomainModelTest {

    @Test
    public void testGetSettingsCatalog() throws Exception {
        ConfigDaoBean mockDao = Mockito.mock(ConfigDaoBean.class);

        // Mock 1 global setting
        ArrayList<Setting> globalSettings = new ArrayList<Setting>(Arrays.asList(MockData.getEntity(3)));
        Mockito.when(mockDao.getGlobalSettings()).thenReturn(globalSettings);

        Module module = new Module();
        module.setModuleName("module1");
        // Mock 2 module settings
        module.setSettings(new ArrayList<Setting>(Arrays.asList(MockData.getEntity(1), MockData.getEntity(2))));

        // Module names = ["module1"]
        Mockito.when(mockDao.getModules()).thenReturn(new ArrayList<Module>(Arrays.asList(module)));

        ConfigDomainModelBean configDomainModelBean = new ConfigDomainModelBean();
        configDomainModelBean.dao = mockDao;

        List<SettingsCatalogEntry> catalog = configDomainModelBean.getSettingsCatalog();
        assertEquals(1, catalog.size());
        assertEquals("module1", catalog.get(0).getModuleName());
        assertEquals(2, catalog.get(0).getSettings().size());
    }

    @Test
    public void testGetSettingsList() throws Exception {
        ConfigDaoBean mockDao = Mockito.mock(ConfigDaoBean.class);

        // Mock 2 module settings
        ArrayList<Setting> moduleSettings = new ArrayList<Setting>(Arrays.asList(MockData.getEntity(1), MockData.getEntity(2)));
        Module module = new Module();
        module.setModuleName("module1");
        module.setSettings(moduleSettings);
        Mockito.when(mockDao.getModuleByName("module1")).thenReturn(module);

        // Mock 1 global setting
        ArrayList<Setting> globalSettings = new ArrayList<Setting>(Arrays.asList(MockData.getEntity(3)));
        Mockito.when(mockDao.getGlobalSettings()).thenReturn(globalSettings);

        ConfigDomainModelBean configDomainModelBean = new ConfigDomainModelBean();
        configDomainModelBean.dao = mockDao;
        List<SettingType> list = configDomainModelBean.getListIncludingGlobal("module1");

        assertEquals(3, list.size());
    }

}