package eu.europa.ec.fisheries.uvms.config.rest.dto;

import java.util.List;
import eu.europa.ec.fisheries.schema.config.types.v1.SettingType;

public class SettingsWithPermissionDto {

    private Boolean hasManagePermission;
    private List<SettingType> settings;

    public SettingsWithPermissionDto(Boolean hasManagePermission, List<SettingType> settings) {
        this.hasManagePermission = hasManagePermission;
        this.settings = settings;
    }


    public Boolean getHasManagePermission() {
        return hasManagePermission;
    }

    public void setHasManagePermission(Boolean hasManagePermission) {
        this.hasManagePermission = hasManagePermission;
    }

    public List<SettingType> getSettings() {
        return settings;
    }

    public void setSettings(List<SettingType> settings) {
        this.settings = settings;
    }
}
