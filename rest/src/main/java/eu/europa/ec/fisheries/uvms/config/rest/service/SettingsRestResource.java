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
package eu.europa.ec.fisheries.uvms.config.rest.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.europa.ec.fisheries.schema.config.types.v1.SettingType;
import eu.europa.ec.fisheries.schema.config.types.v1.SettingsCreateQuery;
import eu.europa.ec.fisheries.uvms.config.rest.dto.ResponseCode;
import eu.europa.ec.fisheries.uvms.config.rest.dto.ResponseDto;
import eu.europa.ec.fisheries.uvms.config.rest.mapper.ModuleStatusMapper;
import eu.europa.ec.fisheries.uvms.config.service.exception.ServiceException;
import eu.europa.ec.fisheries.uvms.rest.security.RequiresFeature;
import eu.europa.ec.fisheries.uvms.rest.security.UnionVMSFeature;

@Stateless
@Path("/")
@RequiresFeature(UnionVMSFeature.viewConfiguration)
public class SettingsRestResource {

    final static Logger LOG = LoggerFactory.getLogger(SettingsRestResource.class);

    @EJB
    ConfigService serviceLayer;

    @Context
    private HttpServletRequest request;

    /**
     * @param query an object containing the setting and the name of the module
     * @return the created setting
     * @summary Creates a new setting.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @POST
    @Consumes(value = { MediaType.APPLICATION_JSON })
    @Produces(value = { MediaType.APPLICATION_JSON })
    @Path("/settings")
    public ResponseDto create(SettingsCreateQuery query) {
        LOG.info("Create setting invoked in rest layer:{}",query);
        try {
            SettingType setting = serviceLayer.create(query.getSetting(), query.getModuleName(), request.getRemoteUser());
            return new ResponseDto(setting, ResponseCode.OK);
        }
        catch (ServiceException | NullPointerException e) {
            LOG.error("[ Error when creating setting:{} ] {} ",query, e.getMessage());
            return new ResponseDto(e.getMessage(), ResponseCode.ERROR);
        }
    }

    /**
     * @param settingId setting ID
     * @return setting with given ID
     * @summary Returns the setting with the given ID.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @GET
    @Produces(value = { MediaType.APPLICATION_JSON })
    @Path("/settings/{id}")
    public ResponseDto getById(@PathParam(value = "id") final Long settingId) {
        LOG.info("Get setting by ID invoked in rest layer: {}",settingId);
        try {
            return new ResponseDto(serviceLayer.getById(settingId), ResponseCode.OK);
        }
        catch (ServiceException | NullPointerException e) {
            LOG.error("[ Error when getting setting by ID. {}] {} ",settingId, e.getMessage());
            return new ResponseDto(e.getMessage(), ResponseCode.ERROR);
        }
    }

    /**
     * Returns either a list of settings for not-null module, or a map from
     * module name to list of settings if argument module is null.
     * 
     * @param moduleName name of a module
     * @return settings for one or more modules
     * @summary Returns a list of settings for a module.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @GET
    @Produces(value = { MediaType.APPLICATION_JSON })
    @Path("/settings")
    public ResponseDto getByModuleName(@QueryParam("moduleName") String moduleName) {
        LOG.info("Get settings invoked in rest layer:{}",moduleName);
        try {
            List<SettingType> settings = serviceLayer.getList(moduleName);
            if (settings == null) {
                return new ResponseDto("No module called " + moduleName + " exists.", ResponseCode.ERROR);
            }

            return new ResponseDto(settings, ResponseCode.OK);
        }
        catch (ServiceException | NullPointerException ex) {
            LOG.error("[ Error when getting settings list. {} ] {} ",moduleName, ex);
            return new ResponseDto(ex.getMessage(), ResponseCode.ERROR);
        }
    }

    /**
     * @param settingId the ID of the setting to be updated
     * @param setting the setting itself
     * @return the updated setting
     * @summary Updates an existing setting.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @PUT
    @Consumes(value = { MediaType.APPLICATION_JSON })
    @Produces(value = { MediaType.APPLICATION_JSON })
    @Path("/settings/{id}")
    public ResponseDto update(@PathParam(value = "id") Long settingId, final SettingType setting) {
        LOG.info("Update setting invoked in rest layer. {} {}",settingId,setting);
        try {
            return new ResponseDto(serviceLayer.update(settingId, setting, request.getRemoteUser()), ResponseCode.OK);
        }
        catch (ServiceException | NullPointerException e) {
            LOG.error("[ Error when updating setting. {} {}] {} ",settingId,setting, e.getMessage());
            return new ResponseDto(e.getMessage(), ResponseCode.ERROR);
        }
    }

    /**
     * @summary Deletes a setting.
     * @param id the ID of the setting to delete
     * @return the deleted setting
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @DELETE
    @Produces(value = { MediaType.APPLICATION_JSON })
    @Path("/settings/{id}")
    public ResponseDto delete(@PathParam(value = "id") Long id) {
        LOG.info("Delete setting invoked in rest layer. {}",id);
        try {
            return new ResponseDto(serviceLayer.delete(id, request.getRemoteUser()), ResponseCode.OK);
        }
        catch (ServiceException | NullPointerException e) {
            LOG.error("[ Error when updating setting. {}] {} ",id, e.getMessage());
            return new ResponseDto(e.getMessage(), ResponseCode.ERROR);
        }
    }

    /**
     * @summary Returns a catalog of settings, grouped by module.
     * @return a mapping of modules to lists of their settings
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @GET
    @Produces(value = {MediaType.APPLICATION_JSON})
    @Path("/catalog")
    public ResponseDto catalog() {
        try {
            return new ResponseDto(serviceLayer.getCatalog(), ResponseCode.OK);
        }
        catch (ServiceException | NullPointerException ex) {
            LOG.error("[ Error when getting catalog. ] {} ", ex.getMessage());
            return new ResponseDto(ex.getMessage(), ResponseCode.ERROR);
        }
    }

    /**
     * @summary Returns an object of module statuses (online and last ping).
     * @return object of module statuses
     */
    @GET
    @Produces(value = {MediaType.APPLICATION_JSON})
    @Path("/pings")
    public ResponseDto getPings() {
        Map<String, Date> timestamps = serviceLayer.getModuleTimestamps();
        return new ResponseDto(ModuleStatusMapper.mapToModuleStatus(timestamps), ResponseCode.OK);
    }

    /**
     * @summary Returns all global settings.
     * @return global settings
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @GET
    @Produces(value = {MediaType.APPLICATION_JSON})
    @Path("/globals")
    public ResponseDto getGlobalSettings() {
        try {
            return new ResponseDto(serviceLayer.getGlobalSettings(), ResponseCode.OK);
        } catch (ServiceException e) {
            LOG.error("[ Error when getting global settings. ] {} ", e.getMessage());
            return new ResponseDto(e.getMessage(), ResponseCode.ERROR);
        }
    }

}