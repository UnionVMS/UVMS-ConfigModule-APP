package eu.europa.ec.fisheries.uvms.config.service;

import java.util.Map;

public interface DeployedModuleCatalog {
    void setModuleVersion(String moduleName, String version);

    Map<String, Object> getDeployedModuleVersions();

    String getDeployedModuleVersion(String moduleName);
}
