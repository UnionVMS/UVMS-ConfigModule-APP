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

import java.util.List;

import javax.jms.JMSException;
import javax.jms.TextMessage;

import eu.europa.ec.fisheries.schema.config.module.v1.PullSettingsResponse;
import eu.europa.ec.fisheries.schema.config.module.v1.PushSettingsResponse;
import eu.europa.ec.fisheries.schema.config.module.v1.SettingsListResponse;
import eu.europa.ec.fisheries.schema.config.module.v1.SingleSettingResponse;
import eu.europa.ec.fisheries.schema.config.types.v1.ConfigFault;
import eu.europa.ec.fisheries.schema.config.types.v1.PullSettingsStatus;
import eu.europa.ec.fisheries.schema.config.types.v1.SettingType;
import eu.europa.ec.fisheries.uvms.config.model.exception.ModelMapperException;
import eu.europa.ec.fisheries.uvms.config.model.exception.ModelMarshallException;

public class ModuleResponseMapper {

    private static void validateResponse(TextMessage response, String correlationId) throws ModelMapperException, JMSException {

        if (response == null) {
            throw new ModelMapperException("Error when validating response in ResponseMapper: Response is Null");
        }

        if (response.getJMSCorrelationID() == null) {
            throw new ModelMapperException("No corelationId in response (Null) . Expected was: " + correlationId);
        }

        if (!correlationId.equalsIgnoreCase(response.getJMSCorrelationID())) {
            throw new ModelMapperException("Wrong corelationId in response. Expected was: " + correlationId + "But actual was: " + response.getJMSCorrelationID());
        }

        try {
            ConfigFault fault = JAXBMarshaller.unmarshallTextMessage(response, ConfigFault.class);
            throw new ModelMapperException(fault.getMessage());
        } catch (ModelMarshallException e) {
            // Expected exception
        }

    }

    public static List<SettingType> getSettingsFromPullSettingsResponse(TextMessage message) throws ModelMapperException, JMSException {
        validateResponse(message, message.getJMSCorrelationID());
        PullSettingsResponse response = JAXBMarshaller.unmarshallTextMessage(message, PullSettingsResponse.class);
        if (response.getStatus() == PullSettingsStatus.MISSING) {
            return null;
        }

        return response.getSettings();
    }

    public static String toPullSettingsResponse(List<SettingType> settings, PullSettingsStatus status) throws ModelMarshallException {
        PullSettingsResponse response = new PullSettingsResponse();
        response.getSettings().addAll(settings);
        response.setStatus(status);
        return JAXBMarshaller.marshallJaxBObjectToString(response);
    }

    public static String toPushSettingsResponse(List<SettingType> settings) throws ModelMarshallException {
        PushSettingsResponse response = new PushSettingsResponse();
        response.setStatus(PullSettingsStatus.OK);
        response.getSettings().addAll(settings);
        return JAXBMarshaller.marshallJaxBObjectToString(response);
    }

    public static String toSingleSettingResponse(SettingType setting) throws ModelMarshallException {
        SingleSettingResponse response = new SingleSettingResponse();
        response.setSetting(setting);
        return JAXBMarshaller.marshallJaxBObjectToString(response);
    }

    public static String toSettingsListResponse(List<SettingType> settings) throws ModelMarshallException {
        SettingsListResponse response = new SettingsListResponse();
        response.getSettings().addAll(settings);
        return JAXBMarshaller.marshallJaxBObjectToString(response);
    }

    public static List<SettingType> getSettingsFromSettingsListResponse(TextMessage message) throws ModelMapperException, JMSException {
        validateResponse(message, message.getJMSCorrelationID());
        SettingsListResponse response = JAXBMarshaller.unmarshallTextMessage(message, SettingsListResponse.class);
        return response.getSettings();
    }

}