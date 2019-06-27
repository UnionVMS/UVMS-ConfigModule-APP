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
package eu.europa.ec.fisheries.uvms.config.model.mapper;

import eu.europa.ec.fisheries.schema.config.module.v1.*;
import eu.europa.ec.fisheries.schema.config.types.v1.SettingType;
import eu.europa.ec.fisheries.uvms.config.model.exception.ModelMarshallException;

import java.util.List;

public class ModuleRequestMapper {
	
    public static String toPullSettingsRequest(String moduleName) {
        PullSettingsRequest request = new PullSettingsRequest();
        request.setMethod(ConfigModuleMethod.PULL);
        request.setModuleName(moduleName);
        return JAXBMarshaller.marshallJaxBObjectToString(request);
    }

    public static String toPushSettingsRequest(String moduleName, List<SettingType> settings, String username) {
        PushSettingsRequest request = new PushSettingsRequest();
        request.setMethod(ConfigModuleMethod.PUSH);
        request.setUsername(username);
        request.getSettings().addAll(settings);
        request.setModuleName(moduleName);
        return JAXBMarshaller.marshallJaxBObjectToString(request);
    }
    
    public static String toSetSettingRequest(String module, SettingType setting, String username) {
    	SetSettingRequest request = new SetSettingRequest();
    	request.setMethod(ConfigModuleMethod.SET);
    	request.setModule(module);
    	request.setSetting(setting);
        request.setUsername(username);
    	return JAXBMarshaller.marshallJaxBObjectToString(request);
    }
    
    public static String toResetSettingRequest(SettingType setting) {
    	ResetSettingRequest request = new ResetSettingRequest();
    	request.setMethod(ConfigModuleMethod.RESET);
    	request.setSetting(setting);
    	return JAXBMarshaller.marshallJaxBObjectToString(request);
    }
    
    public static String toConfigDeployedMessage() {
        ConfigModuleStatusMessage message = new ConfigModuleStatusMessage();
        message.setStatus(ConfigModuleStatus.DEPLOYED);
        return JAXBMarshaller.marshallJaxBObjectToString(message);
    }

    public static String toSetSettingEventRequest(SettingType setting) {
        return JAXBMarshaller.marshallJaxBObjectToString(ModuleRequestMapper.createSettingEventMessage(setting, SettingEventType.SET));
    }

    public static String toResetSettingEventRequest(SettingType setting) {
        return JAXBMarshaller.marshallJaxBObjectToString(ModuleRequestMapper.createSettingEventMessage(setting, SettingEventType.RESET));
    }

    public static String toPingRequest(String moduleName) {
        PingRequest request = new PingRequest();
        request.setMethod(ConfigModuleMethod.PING);
        request.setModuleName(moduleName);
        return JAXBMarshaller.marshallJaxBObjectToString(request);
    }

    public static String toListSettingsRequest(String moduleName) {
        ListSettingsRequest request = new ListSettingsRequest();
        request.setMethod(ConfigModuleMethod.LIST);
        request.setModuleName(moduleName);
        return JAXBMarshaller.marshallJaxBObjectToString(request);
    }

    private static PushModuleSettingMessage createSettingEventMessage(SettingType setting, SettingEventType eventType) {
        PushModuleSettingMessage request = new PushModuleSettingMessage();
        request.setStatus(ConfigModuleStatus.SETTING_CHANGED);
        request.setAction(eventType);
        request.setSetting(setting);
        return request;
    }

}