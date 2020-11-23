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

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

import eu.europa.ec.fisheries.uvms.config.service.DeployedModuleCatalog;

@ApplicationScoped
public class DeployedModuleCatalogBean implements DeployedModuleCatalog {

    public static final String PLATFORM_VERSION_PROPERTIES_KEY = "uvms.platform.version";

    @Inject
    private PropertiesBean propertiesBean;

    private Map<String, Object> deployedModuleVersions = new HashMap<>();

    private DeployedModuleCatalogBean() {
    }

    @PostConstruct
    void init() {
        deployedModuleVersions.put("platformVersion", propertiesBean.getProperty(PLATFORM_VERSION_PROPERTIES_KEY));
        deployedModuleVersions.put("modules", new HashMap<>());
    }

    @Override
    public synchronized void setModuleVersion(String moduleName, String version) {
        Map<String, String> modules = (Map<String, String>) deployedModuleVersions.get("modules");
        modules.put(moduleName, version);
    }

    @Override
    public synchronized Map<String, Object> getDeployedModuleVersions() {
        return deployedModuleVersions;
    }

    @Override
    public synchronized String getDeployedModuleVersion(String moduleName) {
        Map<String, String> modules = (Map<String, String>) deployedModuleVersions.get("modules");
        return modules.get("moduleName");
    }
}