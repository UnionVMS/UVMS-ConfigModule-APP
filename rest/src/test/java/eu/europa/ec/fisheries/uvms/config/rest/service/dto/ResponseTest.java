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
package eu.europa.ec.fisheries.uvms.config.rest.service.dto;

import eu.europa.ec.fisheries.uvms.commons.rest.dto.ResponseDto;
import eu.europa.ec.fisheries.uvms.config.rest.dto.ResponseCode;
import junit.framework.Assert;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ResponseTest {

    public ResponseTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void checkDtoReturnsValid() {

        String VALUE = "HELLO_DTO";
        ResponseDto dto = new ResponseDto(VALUE, ResponseCode.OK.getCode());
        Assert.assertEquals(dto.getCode(), ResponseCode.OK.getCode());
        Assert.assertEquals(dto.getData(), VALUE);

        dto = new ResponseDto(ResponseCode.ERROR.getCode());
        Assert.assertEquals(dto.getCode(), ResponseCode.ERROR.getCode());
        Assert.assertEquals(dto.getData(), null);

    }
}