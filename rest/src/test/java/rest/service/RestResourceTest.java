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
package rest.service;

import static org.junit.Assert.*;
import static org.mockito.Mockito.doReturn;

import java.util.List;

import eu.europa.ec.fisheries.uvms.config.service.bean.ConfigServiceBean;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import eu.europa.ec.fisheries.schema.config.types.v1.SettingType;
import eu.europa.ec.fisheries.schema.config.types.v1.SettingsCreateQuery;
import eu.europa.ec.fisheries.uvms.config.rest.service.SettingsRestResource;
import eu.europa.ec.fisheries.uvms.config.rest.dto.ResponseCode;
import eu.europa.ec.fisheries.uvms.config.rest.dto.ResponseDto;
import eu.europa.ec.fisheries.uvms.config.service.mockdata.MockData;

import javax.servlet.http.HttpServletRequest;

public class RestResourceTest {

    private static final Long ID = 1L;
    private static final Integer VESSEL_LIST_SIZE = 3;

    List<SettingType> DTO_LIST = MockData.getDtoList(VESSEL_LIST_SIZE);
    SettingType DTO = MockData.getDto(ID);

    private final ResponseDto ERROR_RESULT;
    private final ResponseDto SUCCESS_RESULT;
    private final ResponseDto SUCCESS_RESULT_LIST;
    private final ResponseDto SUCCESS_RESULT_DTO;

    SettingsRestResource SERVICE_NULL = new SettingsRestResource();

    @Mock
    ConfigServiceBean serviceLayer;

    @Mock
    HttpServletRequest request;

    @InjectMocks
    SettingsRestResource settingsRestResource;

    public RestResourceTest() {
        ERROR_RESULT = new ResponseDto(ResponseCode.ERROR);
        SUCCESS_RESULT = new ResponseDto(ResponseCode.OK);
        SUCCESS_RESULT_LIST = new ResponseDto(DTO_LIST, ResponseCode.OK);
        SUCCESS_RESULT_DTO = new ResponseDto(DTO, ResponseCode.OK);
    }

    @BeforeClass
    public static void setUpClass() {

    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @After
    public void tearDown() {
    }

    /**
     * Test get list with a happy outcome
     *
     */
    @Test
    public void testGetVesselList() {
        doReturn(DTO_LIST).when(serviceLayer).getList("apa");
        ResponseDto result = settingsRestResource.getByModuleName("apa");
        assertEquals(SUCCESS_RESULT_LIST.toString(), result.toString());
    }

    /**
     * Test get list when the injected EJB is null
     *
     */
    @Test
    public void testGetVesselListNull() {
        ResponseDto result = SERVICE_NULL.getByModuleName("apa");
        assertEquals(ERROR_RESULT.toString(), result.toString());
    }

    /**
     * Test get by id with a happy outcome
     *
     */
    @Test
    public void testGetVesselById() {
        doReturn(DTO).when(serviceLayer).getById(ID);
        ResponseDto result = settingsRestResource.getById(ID);
        Mockito.verify(serviceLayer).getById(ID);
        assertEquals(SUCCESS_RESULT_DTO.toString(), result.toString());

    }

    /**
     * Test get by id when the injected EJB is null
     *
     */
    @Test
    public void testGetVesselByIdNull() {
        ResponseDto result = SERVICE_NULL.getById(ID);
        assertEquals(ERROR_RESULT.toString(), result.toString());
    }

    /**
     * Test create with a happy outcome
     *
     */
    @Test
    public void testCreateVessel() {
        SettingsCreateQuery query = new SettingsCreateQuery();
        query.setModuleName("apa");
        query.setSetting(DTO);
        doReturn("testUsername").when(request).getRemoteUser();
        ResponseDto result = settingsRestResource.create(query);
        Mockito.verify(serviceLayer).create(DTO, "apa", "testUsername");
        assertEquals(SUCCESS_RESULT.toString(), result.toString());
    }

    /**
     * Test create when the injected EJB is null
     */
    @Test
    public void testCreateVesselNull() {
        SettingsCreateQuery query = new SettingsCreateQuery();
        query.setModuleName("apa");
        query.setSetting(DTO);
        ResponseDto result = SERVICE_NULL.create(query);
        assertEquals(ERROR_RESULT.toString(), result.toString());
    }

    /**
     * Test update with a happy outcome
     *
     */
    @Test
    public void testUpdateVessel() {
        doReturn("testUsername").when(request).getRemoteUser();
        ResponseDto result = settingsRestResource.update(1L, DTO);
        Mockito.verify(serviceLayer).update(1L, DTO, "testUsername");
        assertEquals(SUCCESS_RESULT.toString(), result.toString());
    }

    /**
     * Test update when the injected EJB is null
     */
    @Test
    public void testUpdateVesselNull() {
        ResponseDto result = SERVICE_NULL.update(1L, DTO);
        assertEquals(ERROR_RESULT.toString(), result.toString());
    }

}