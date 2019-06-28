package eu.europa.ec.fisheries.uvms.config.rest.service;

import eu.europa.ec.fisheries.uvms.config.rest.BuildConfigRestTestDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertTrue;


@RunWith(Arquillian.class)
public class ConfigRestTests extends BuildConfigRestTestDeployment {

    @Test
    public void worldsBestAndMostUsefullRestTest(){
        assertTrue(true);
    }
}
