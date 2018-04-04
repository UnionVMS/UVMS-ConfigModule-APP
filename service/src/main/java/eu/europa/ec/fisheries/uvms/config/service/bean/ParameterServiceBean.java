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

import eu.europa.ec.fisheries.uvms.config.service.ParameterService;
import eu.europa.ec.fisheries.uvms.config.service.config.ParameterKey;
import eu.europa.ec.fisheries.uvms.config.service.entity.Parameter;
import eu.europa.ec.fisheries.uvms.config.service.constants.ServiceConstants;
import eu.europa.ec.fisheries.uvms.config.service.exception.InputArgumentException;
import eu.europa.ec.fisheries.uvms.config.service.exception.ServiceException;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Stateless
public class ParameterServiceBean implements ParameterService {

    /**
     * THIS BEAN NEEDS TO HAVE ITS NAME CHANGED SO IT DOES NOT CONFLICT WITH
     * OTHER COMPONENTS
     */
    final static Logger LOG = LoggerFactory.getLogger(ParameterServiceBean.class);

    @PersistenceContext(unitName = "config")
    private EntityManager em;

    @Override
    public String getStringValue(ParameterKey key) throws ServiceException {
        try {
            Query query = em.createNamedQuery(ServiceConstants.FIND_BY_NAME);
            query.setParameter("key", key.getKey());
            Parameter entity = (Parameter) query.getSingleResult();
            return entity.getParamValue();
        } catch (Exception ex) {
            LOG.error("[ Error when getting String value ]", ex.getMessage());
            throw new ServiceException("[ Error when getting String value ]", ex);
        }
    }

    @Override
    public Boolean getBooleanValue(ParameterKey key) throws ServiceException {
        try {
            Query query = em.createNamedQuery(ServiceConstants.FIND_BY_NAME);
            query.setParameter("key", key.getKey());
            Parameter entity = (Parameter) query.getSingleResult();
            return parseBooleanValue(entity.getParamValue());
        } catch (ServiceException ex) {
            LOG.error("[ Error when getting Boolean value ]", ex.getMessage());
            throw new ServiceException("[ Error when getting Boolean value ]", ex);
        }
    }

    private Boolean parseBooleanValue(String value) throws InputArgumentException, ServiceException {
        try {
            if (value.equalsIgnoreCase("true")) {
                return Boolean.TRUE;
            } else if (value.equalsIgnoreCase("false")) {
                return Boolean.FALSE;
            } else {
                LOG.error("[ Error when parsing Boolean value from String, The String provided dows not equal 'TRUE' or 'FALSE'. The value is {} ]", value);
                throw new InputArgumentException("The String value provided does not equal boolean value, value provided = " + value);
            }
        } catch (Exception ex) {
            LOG.error("[ Error when parsing Boolean value from String ]", ex.getMessage());
            throw new ServiceException("[ Error when parsing Boolean value from String ]", ex);
        }
    }

}