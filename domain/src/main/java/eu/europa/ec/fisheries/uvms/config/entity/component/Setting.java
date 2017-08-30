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
package eu.europa.ec.fisheries.uvms.config.entity.component;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import eu.europa.ec.fisheries.uvms.config.constant.UvmsConstants;

@Entity
@Table(name = "settings")
@NamedQueries({
    @NamedQuery(name = UvmsConstants.SETTING_FIND_GLOBALS, query = "SELECT s FROM Setting s WHERE s.global = true"),
    @NamedQuery(name = UvmsConstants.SETTING_FIND_GLOBAL_BY_KEY, query = "SELECT s FROM Setting s WHERE s.global = true and s.key = :key"),
    @NamedQuery(name = UvmsConstants.SETTING_FIND_BY_KEY_AND_MODULE, query = "SELECT s FROM Setting s WHERE s.key = :key and s.module.moduleName = :moduleName")
})
public class Setting implements Serializable {

    private static final long serialVersionUID = 1L;


    @Id
    @GeneratedValue(strategy =  GenerationType.AUTO)
    @Column(name = "setting_id")
    private Long id;

    @Column(name = "setting_key")
    private String key;

    @Column(name = "setting_value")
    private String value;

    @Column(name = "setting_description")
    private String description;

    @Column(name = "global")
    private boolean global;

    @Column(name = "setting_last_modified")
    private Date lastModified;

    @Size(max=60)
    @NotNull
    @Column(name="updated_by")
    private String updatedBy;

    @ManyToOne
    @JoinColumn(name = "setting_module_id")
    private Module module;

    public Setting() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isGlobal() {
        return global;
    }

    public void setGlobal(boolean global) {
        this.global = global;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public Module getModule() {
        return module;
    }

    public void setModule(Module module) {
        this.module = module;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

}