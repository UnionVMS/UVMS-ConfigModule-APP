package eu.europa.ec.fisheries.uvms.config.rest;

import java.util.Arrays;
import javax.ejb.EJB;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.europa.ec.fisheries.uvms.commons.date.JsonBConfigurator;
import eu.europa.ec.fisheries.uvms.config.service.BuildConfigServiceTestDeployment;
import eu.europa.ec.fisheries.uvms.rest.security.UnionVMSFeature;
import eu.europa.ec.mare.usm.jwt.JwtTokenHandler;

public abstract class BuildConfigRestTestDeployment extends BuildConfigServiceTestDeployment {

    final static Logger LOG = LoggerFactory.getLogger(BuildConfigRestTestDeployment.class);

    @EJB
    private JwtTokenHandler tokenHandler;

    private String token;

    protected WebTarget getWebTarget() {

        Client client = ClientBuilder.newClient();
        client.register(JsonBConfigurator.class);
        //return client.target("http://localhost:28080/test/rest");
        return client.target("http://localhost:8080/test/rest");
    }

    protected String getToken() {
        if (token == null) {
            token = tokenHandler.createToken("user",
                    Arrays.asList(UnionVMSFeature.viewConfiguration.getFeatureId()));
        }
        return token;
    }
}
