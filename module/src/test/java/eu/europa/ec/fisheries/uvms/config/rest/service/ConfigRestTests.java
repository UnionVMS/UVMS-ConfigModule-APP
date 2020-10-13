package eu.europa.ec.fisheries.uvms.config.rest.service;

import eu.europa.ec.fisheries.schema.config.module.v1.ConfigModuleMethod;
import eu.europa.ec.fisheries.schema.config.types.v1.SettingType;
import eu.europa.ec.fisheries.schema.config.types.v1.SettingsCreateQuery;
import eu.europa.ec.fisheries.uvms.commons.date.JsonBConfigurator;
import eu.europa.ec.fisheries.uvms.config.model.mapper.ModuleRequestMapper;
import eu.europa.ec.fisheries.uvms.config.rest.BuildConfigRestTestDeployment;
import eu.europa.ec.fisheries.uvms.config.rest.JMSHelper;
import eu.europa.ec.fisheries.uvms.config.rest.entity.ModuleStatus;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.annotation.Resource;
import javax.jms.ConnectionFactory;
import javax.json.bind.Jsonb;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.*;


@RunWith(Arquillian.class)
public class ConfigRestTests extends BuildConfigRestTestDeployment {

    @Resource(mappedName = "java:/ConnectionFactory")
    private ConnectionFactory connectionFactory;

    private static Jsonb jsonb = new JsonBConfigurator().getContext(null);

    JMSHelper jmsHelper;

    @Before
    public void cleanJMS() {
        jmsHelper = new JMSHelper(connectionFactory);
    }

    @Test
    public void worldsBestAndMostUsefulRestTest(){
        assertTrue(true);
    }

    @Test
    public void createSettingByRest() {
        SettingsCreateQuery query = new SettingsCreateQuery();
        String moduleName = "Rest test module " + UUID.randomUUID().getLeastSignificantBits();
        query.setModuleName(moduleName);
        SettingType setting = createBasicSetting(moduleName);
        query.setSetting(setting);

        SettingType createdSetting = createSettingByRest(query);

        assertNotNull(createdSetting.getId());
        assertEquals(setting.isGlobal(), createdSetting.isGlobal());
        assertEquals(setting.getKey(), createdSetting.getKey());
        assertEquals(setting.getValue(), createdSetting.getValue());
        assertEquals(setting.getDescription(), createdSetting.getDescription());
        assertEquals(setting.getModule(), createdSetting.getModule());
    }


    @Test
    public void getSettingById() {
        SettingsCreateQuery query = new SettingsCreateQuery();
        String moduleName = "Rest test module " + UUID.randomUUID().getLeastSignificantBits();
        query.setModuleName(moduleName);
        SettingType setting = createBasicSetting(moduleName);
        query.setSetting(setting);

        SettingType createdSetting = createSettingByRest(query);

        SettingType responseSetting = getWebTarget()
                .path("settings")
                .path("" + createdSetting.getId())
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getToken())
                .get(SettingType.class);

        assertEquals(createdSetting.getId(), responseSetting.getId());
        assertEquals(setting.isGlobal(), responseSetting.isGlobal());
        assertEquals(setting.getKey(), responseSetting.getKey());
        assertEquals(setting.getValue(), responseSetting.getValue());
        assertEquals(setting.getDescription(), responseSetting.getDescription());
        assertEquals(setting.getModule(), responseSetting.getModule());
    }

    @Test
    public void getByModuleName() {
        String moduleName = "Rest test module " + UUID.randomUUID().getLeastSignificantBits();
        SettingsCreateQuery query1 = new SettingsCreateQuery();
        SettingsCreateQuery query2 = new SettingsCreateQuery();

        query1.setModuleName(moduleName);
        query2.setModuleName(moduleName);
        SettingType setting1 = createBasicSetting(moduleName);
        SettingType setting2 = createBasicSetting(moduleName);
        setting2.setKey(setting2.getKey() + " 2");
        query1.setSetting(setting1);
        query2.setSetting(setting2);

        SettingType createdSetting1 = createSettingByRest(query1);
        SettingType createdSetting2 = createSettingByRest(query2);

        List<SettingType> moduleSettings = getWebTarget()
                .path("settings")
                .queryParam("moduleName", moduleName)
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getToken())
                .get(new GenericType<List<SettingType>>() {});
        assertEquals(15, moduleSettings.size());        ////the two we created plus 13 global ones.......
    }

    @Test
    public void updateSettingTest() {
        SettingsCreateQuery query = new SettingsCreateQuery();
        String moduleName = "Rest test module " + UUID.randomUUID().getLeastSignificantBits();
        query.setModuleName(moduleName);
        SettingType setting = createBasicSetting(moduleName);
        query.setSetting(setting);

        SettingType createdSetting = createSettingByRest(query);
        createdSetting.setValue("Updated Value");

        SettingType updatedSetting = getWebTarget()
                .path("settings")
                .path("" + createdSetting.getId())
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getToken())
                .put(Entity.json(createdSetting), SettingType.class);

        assertEquals(createdSetting.getId(), updatedSetting.getId());
        assertEquals("Updated Value", updatedSetting.getValue());
    }

    @Test
    public void updateSettingViaKeyValue() {
        SettingsCreateQuery query = new SettingsCreateQuery();
        String moduleName = "Rest test module " + UUID.randomUUID().getLeastSignificantBits();
        query.setModuleName(moduleName);
        SettingType setting = createBasicSetting(moduleName);
        query.setSetting(setting);

        SettingType createdSetting = createSettingByRest(query);
        String updatedValue = "Updated Value";

        Response response = getWebTarget()
                .path("settings")
                .path(moduleName)
                .path(setting.getKey())
                .path(updatedValue)
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getToken())
                .put(Entity.json(""), Response.class);

        assertEquals(200, response.getStatus());

        SettingType updatedSetting = response.readEntity(SettingType.class);
        assertEquals(createdSetting.getId(), updatedSetting.getId());
        assertEquals(moduleName, updatedSetting.getModule());
        assertEquals(updatedValue, updatedSetting.getValue());
    }

    @Test
    public void getGlobalSettings() {
        List<SettingType> globalSetting = getWebTarget()
                .path("globals")
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getToken())
                .get(new GenericType<List<SettingType>>() {});
        assertEquals(13, globalSetting.size());
    }

    @Test
    public void createAndDeleteGlobalSetting() {
        SettingsCreateQuery query = new SettingsCreateQuery();
        SettingType setting = createBasicSetting(null);
        setting.setGlobal(true);
        query.setSetting(setting);

        SettingType createdSetting = createSettingByRest(query);
        assertNotNull(createdSetting.getId());

        List<SettingType> globalSetting = getWebTarget()
                .path("globals")
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getToken())
                .get(new GenericType<List<SettingType>>() {});
        assertEquals(14, globalSetting.size());
        assertTrue(globalSetting.stream().anyMatch( s -> s.getId().equals(createdSetting.getId())));

        Response response = getWebTarget()
                .path("settings")
                .path("" + createdSetting.getId())
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getToken())
                .delete(Response.class);
        assertEquals(200, response.getStatus());

        globalSetting = getWebTarget()
                .path("globals")
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getToken())
                .get(new GenericType<List<SettingType>>() {});
        assertEquals(13, globalSetting.size());
        assertFalse(globalSetting.stream().anyMatch( s -> s.getId().equals(createdSetting.getId())));
    }

    @Test
    public void getCatalog() {
        String moduleName1 = "Rest test module " + UUID.randomUUID().getLeastSignificantBits();
        String moduleName2 = "Rest test module " + UUID.randomUUID().getLeastSignificantBits();
        String moduleName3 = "Rest test module " + UUID.randomUUID().getLeastSignificantBits();
        SettingsCreateQuery query1 = new SettingsCreateQuery();
        SettingsCreateQuery query2 = new SettingsCreateQuery();
        SettingsCreateQuery query3 = new SettingsCreateQuery();

        query1.setModuleName(moduleName1);
        query2.setModuleName(moduleName2);
        query3.setModuleName(moduleName3);
        SettingType setting1 = createBasicSetting(moduleName1);
        SettingType setting2 = createBasicSetting(moduleName2);
        SettingType setting3 = createBasicSetting(moduleName3);
        query1.setSetting(setting1);
        query2.setSetting(setting2);
        query3.setSetting(setting3);

        SettingType createdSetting1 = createSettingByRest(query1);
        SettingType createdSetting2 = createSettingByRest(query2);
        SettingType createdSetting3 = createSettingByRest(query3);


        Map<String, List<SettingType>> catalog = getWebTarget()
                .path("catalog")
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getToken())
                .get(new GenericType<Map<String, List<SettingType>>>() {});

        assertTrue(catalog.size() > 2 );
        assertTrue(catalog.containsKey(moduleName1));
        assertTrue(catalog.containsKey(moduleName2));
        assertTrue(catalog.containsKey(moduleName3));
    }

    @Test
    public void getPings() throws Exception{
        Instant timestamp = Instant.now();
        sendPingFromModule("Heartbeat rest 1");
        sendPingFromModule("Heartbeat rest 2");
        sendPingFromModule("Heartbeat rest 3");
        Thread.sleep(500);

        Map<String, ModuleStatus> pings = getWebTarget()
                .path("pings")
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getToken())
                .get(new GenericType<Map<String , ModuleStatus>>() {});

        assertTrue(pings.size() > 2 );
        assertTrue(pings.containsKey("Heartbeat rest 1"));
        assertTrue(pings.get("Heartbeat rest 1").isOnline());
        assertTrue(Instant.parse(pings.get("Heartbeat rest 1").getLastPing()).isAfter(timestamp));
        assertTrue(pings.containsKey("Heartbeat rest 2"));
        assertTrue(pings.get("Heartbeat rest 2").isOnline());
        assertTrue(Instant.parse(pings.get("Heartbeat rest 2").getLastPing()).isAfter(timestamp));
        assertTrue(pings.containsKey("Heartbeat rest 3"));
        assertTrue(pings.get("Heartbeat rest 3").isOnline());
        assertTrue(Instant.parse(pings.get("Heartbeat rest 3").getLastPing()).isAfter(timestamp));

    }


    private SettingType createBasicSetting(String moduleName) {

        SettingType setting = new SettingType();
        setting.setGlobal(false);
        setting.setModule(moduleName);
        setting.setKey(moduleName + " key");
        setting.setValue(moduleName + " value");
        setting.setDescription(moduleName + " description");

        return setting;
    }

    private SettingType createSettingByRest(SettingsCreateQuery query){
        SettingType response = getWebTarget()
                .path("settings")
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getToken())
                .post(Entity.json(query), SettingType.class);

        return response;
    }

    private void sendPingFromModule(String moduleName) throws Exception {
        String xml = ModuleRequestMapper.toPingRequest(moduleName);
        jmsHelper.sendConfigMessage(xml, ConfigModuleMethod.PING.value());
    }
}
