/*
﻿﻿Developed with the contribution of the European Commission - Directorate General for Maritime Affairs and Fisheries
© European Union, 2015-2016.
 
This file is part of the Integrated Data Fisheries Management (IFDM) Suite. The IFDM Suite is free software: you can
redistribute it and/or modify it under the terms of the GNU General Public License as published by the
Free Software Foundation, either version 3 of the License, or any later version. The IFDM Suite is distributed in
the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details. You should have received a copy
of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.config.rest.mapper;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import eu.europa.ec.fisheries.uvms.config.rest.entity.ModuleStatus;
import eu.europa.ec.fisheries.uvms.config.service.bean.ModuleAvailabilityBean;

public class ModuleStatusMapper {

    public static Map<String, ModuleStatus> mapToModuleStatus(Map<String, Date> lastPings) {
        Map<String, ModuleStatus> map = new HashMap<>();
        for (Entry<String, Date> modulePingEntry : lastPings.entrySet()) {
            map.put(modulePingEntry.getKey(), ModuleStatusMapper.mapToModuleStatus(modulePingEntry.getValue()));
        }
        
        return map;
    }

    public static ModuleStatus mapToModuleStatus(Date lastPing) {
        ModuleStatus status = new ModuleStatus();
        status.setLastPing(lastPing);
        status.setOnline(ModuleAvailabilityBean.isOnline(lastPing));
        return status;
    }

}