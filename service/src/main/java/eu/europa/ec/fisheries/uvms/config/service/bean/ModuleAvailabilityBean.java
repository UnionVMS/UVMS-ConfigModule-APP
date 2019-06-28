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

import javax.ejb.Singleton;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class ModuleAvailabilityBean {

    private static final int FIVE_MINUTES = 300000;
    private Map<String, Instant> timestampByModule;

    public ModuleAvailabilityBean() {
        timestampByModule = new HashMap<>();
    }

    public void setTimestamp(String moduleName, Instant timestamp) {
        timestampByModule.put(moduleName, timestamp);
    }

    public Map<String, Instant> getTimestamps() {
        return new HashMap<>(timestampByModule);
    }

    public static boolean isOnline(Instant timestamp) {
        return Duration.between(Instant.now(), timestamp).minusMillis(FIVE_MINUTES).isNegative();
    }

}