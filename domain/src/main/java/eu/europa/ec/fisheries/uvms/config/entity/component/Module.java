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
import java.util.ArrayList;
import java.util.List;

import javax.persistence.*;

import eu.europa.ec.fisheries.uvms.config.constant.UvmsConstants;

@Entity
@Table(name = "modules")
@NamedQueries({
	@NamedQuery(name = UvmsConstants.MODULE_LIST_ALL, query = "SELECT m FROM Module m"),
	@NamedQuery(name = UvmsConstants.MODULE_FIND_BY_NAME, query = "SELECT m FROM Module m WHERE m.moduleName = :moduleName")
})
public class Module implements Serializable {

    private static final long serialVersionUID = 1L;


    @Id
    @GeneratedValue(strategy =  GenerationType.AUTO)
    @Column(name = "module_id")
    private Long id;

    @Column(name = "module_name")
    private String moduleName;
    
    @OneToMany
    @JoinColumn(name = "setting_module_id")
    private List<Setting> settings;
    
    public Module() {
    }

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getModuleName() {
		return moduleName;
	}

	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}

	public List<Setting> getSettings() {
        if (settings == null) {
            settings = new ArrayList<>();
        }

        return settings;
	}

	public void setSettings(List<Setting> settings) {
		this.settings = settings;
	}

}