package eu.europa.ec.fisheries.uvms.config.service;

import eu.europa.ec.fisheries.schema.config.module.v1.*;
import eu.europa.ec.fisheries.schema.config.types.v1.PullSettingsStatus;
import eu.europa.ec.fisheries.schema.config.types.v1.SettingType;
import eu.europa.ec.fisheries.uvms.config.model.mapper.JAXBMarshaller;
import eu.europa.ec.fisheries.uvms.config.model.mapper.ModuleRequestMapper;
import eu.europa.ec.fisheries.uvms.config.service.bean.ModuleAvailabilityBean;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.jms.ConnectionFactory;
import javax.jms.TextMessage;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;

@RunWith(Arquillian.class)
public class ConfigJMSTests extends BuildConfigServiceTestDeployment {

    @Resource(mappedName = "java:/ConnectionFactory")
    private ConnectionFactory connectionFactory;

    @Inject
    private ModuleAvailabilityBean moduleAvailability;

    JMSHelper jmsHelper;

    @Before
    public void cleanJMS() {
        jmsHelper = new JMSHelper(connectionFactory);
    }


    @Test
    public void worldsBestAndMostUsefullServiceTest(){
        assertTrue(true);
    }

    @Test
    public void pushSettings() throws Exception {
        String moduleName = "Test Module  " + UUID.randomUUID().getLeastSignificantBits();
        String basicSettingXml = createBasicSetting(moduleName);
        TextMessage responseMessage = sendStringToConfigAndReturnResponse(basicSettingXml, ConfigModuleMethod.PUSH);

        PushSettingsResponse response = JAXBMarshaller.unmarshallTextMessage(responseMessage, PushSettingsResponse.class);

        assertEquals(PullSettingsStatus.OK.value(), response.getStatus().value());
        assertEquals(15, response.getSettings().size());    //The settings we just set plus 13 global settings.
        for (SettingType setting : response.getSettings()) {
            assertTrue(setting.getId() != null);
            if(setting.isGlobal()){                                 //dont really want to look at the global ones
                continue;
            }
            assertFalse(setting.getKey().isEmpty());
            assertTrue(basicSettingXml.contains(setting.getKey()));
            assertFalse(setting.getValue().isEmpty());
            assertTrue(basicSettingXml.contains(setting.getValue()));
            assertFalse(setting.getDescription().isEmpty());
            assertTrue(basicSettingXml.contains(setting.getDescription()));
            assertFalse(setting.getModule().isEmpty());
            assertTrue(basicSettingXml.contains(setting.getModule()));
            assertFalse(setting.isGlobal());

        }

    }

    @Test
    public void createAndListSettings() throws Exception{
        String moduleName = "Test Module " + UUID.randomUUID().getLeastSignificantBits();
        String basicSettingXml = createBasicSetting(moduleName);
        sendStringToConfigAndReturnResponse(basicSettingXml, ConfigModuleMethod.PUSH);


        String listXml = ModuleRequestMapper.toListSettingsRequest(moduleName);
        TextMessage responseMessage = sendStringToConfigAndReturnResponse(listXml, ConfigModuleMethod.LIST);

        SettingsListResponse response = JAXBMarshaller.unmarshallTextMessage(responseMessage, SettingsListResponse.class);

        assertEquals(15, response.getSettings().size());    //the two we created plus 13 global ones.......

        for (SettingType setting : response.getSettings()) {
            if(setting.isGlobal()){
                continue;
            }
            assertFalse(setting.getKey().isEmpty());
            assertTrue(basicSettingXml.contains(setting.getKey()));
            assertFalse(setting.getValue().isEmpty());
            assertTrue(basicSettingXml.contains(setting.getValue()));
            assertFalse(setting.getDescription().isEmpty());
            assertTrue(basicSettingXml.contains(setting.getDescription()));
            assertFalse(setting.getModule().isEmpty());
            assertTrue(basicSettingXml.contains(setting.getModule()));
            assertFalse(setting.isGlobal());
        }
    }


    @Test
    public void ping() throws Exception{
        Instant timestamp = Instant.now();
        String moduleName = "Heartbeat module";
        String xml = ModuleRequestMapper.toPingRequest(moduleName);
        jmsHelper.sendConfigMessage(xml, ConfigModuleMethod.PING.value());
        Thread.sleep(500);

        assertTrue(moduleAvailability.getTimestamps().get(moduleName).isAfter(timestamp));
    }

    @Test
    public void pullSettings() throws Exception{
        String moduleName = "Test Module " + UUID.randomUUID().getLeastSignificantBits();
        String basicSettingXml = createBasicSetting(moduleName);
        sendStringToConfigAndReturnResponse(basicSettingXml, ConfigModuleMethod.PUSH);

        String xml = ModuleRequestMapper.toPullSettingsRequest(moduleName);
        TextMessage message = sendStringToConfigAndReturnResponse(xml, ConfigModuleMethod.PULL);
        PullSettingsResponse response = JAXBMarshaller.unmarshallTextMessage(message, PullSettingsResponse.class);

        assertTrue(response.getStatus().equals(PullSettingsStatus.OK));
        assertEquals(15, response.getSettings().size());
        for (SettingType setting : response.getSettings()) {
            if(setting.isGlobal()){
                continue;
            }
            assertFalse(setting.getKey().isEmpty());
            assertTrue(basicSettingXml.contains(setting.getKey()));
            assertFalse(setting.getValue().isEmpty());
            assertTrue(basicSettingXml.contains(setting.getValue()));
            assertFalse(setting.getDescription().isEmpty());
            assertTrue(basicSettingXml.contains(setting.getDescription()));
            assertFalse(setting.getModule().isEmpty());
            assertTrue(basicSettingXml.contains(setting.getModule()));
            assertFalse(setting.isGlobal());
        }

    }

    @Test
    public void pullSettingsNonexistantModule() throws Exception{
        String xml = ModuleRequestMapper.toPullSettingsRequest("Nonexistant module");
        TextMessage message = sendStringToConfigAndReturnResponse(xml, ConfigModuleMethod.PULL);
        PullSettingsResponse response = JAXBMarshaller.unmarshallTextMessage(message, PullSettingsResponse.class);

        assertTrue(response.getStatus().equals(PullSettingsStatus.MISSING));
    }

    @Test
    public void setSetting() throws Exception{
        String moduleName = "Test Module " + UUID.randomUUID().getLeastSignificantBits();
        String username = moduleName + " username";
        SettingType setting = new SettingType();
        setting.setGlobal(false);
        setting.setModule(moduleName);
        setting.setKey(moduleName + " key 1");
        setting.setValue(moduleName + " value 1");
        setting.setDescription(moduleName + " description 1");
        String xml = ModuleRequestMapper.toSetSettingRequest(moduleName, setting, username);

        TextMessage message = sendStringToConfigAndReturnResponse(xml, ConfigModuleMethod.SET);
        SingleSettingResponse response = JAXBMarshaller.unmarshallTextMessage(message, SingleSettingResponse.class);

        SettingType responseSetting = response.getSetting();
        assertNotNull(responseSetting.getId());
        assertEquals(setting.getModule(), responseSetting.getModule());
        assertEquals(setting.getDescription(), responseSetting.getDescription());
        assertEquals(setting.getValue(), responseSetting.getValue());
        assertEquals(setting.getKey(), responseSetting.getKey());
        assertEquals(setting.isGlobal(), responseSetting.isGlobal());
    }

    @Test
    public void resetSettingById() throws Exception{
        String moduleName = "Test Module " + UUID.randomUUID().getLeastSignificantBits();
        String basicSettingXml = createBasicSetting(moduleName);
        TextMessage responseMessage = sendStringToConfigAndReturnResponse(basicSettingXml, ConfigModuleMethod.PUSH);

        PushSettingsResponse response = JAXBMarshaller.unmarshallTextMessage(responseMessage, PushSettingsResponse.class);

        SettingType setting = null;
        for (SettingType s : response.getSettings()) {
            if(!s.isGlobal()){
                setting = s;
            }
        }
        String xml = ModuleRequestMapper.toResetSettingRequest(setting);

        TextMessage message = sendStringToConfigAndReturnResponse(xml, ConfigModuleMethod.RESET);
        SingleSettingResponse ssResponse = JAXBMarshaller.unmarshallTextMessage(message, SingleSettingResponse.class);

        assertNotNull(ssResponse.getSetting());

        String listXml = ModuleRequestMapper.toListSettingsRequest(moduleName);
        TextMessage listMessage = sendStringToConfigAndReturnResponse(listXml, ConfigModuleMethod.LIST);

        SettingsListResponse listResponse = JAXBMarshaller.unmarshallTextMessage(listMessage, SettingsListResponse.class);

        assertEquals(14, listResponse.getSettings().size());    //created two, plus 13 global and then minus one

    }

    @Test
    public void resetSettingByNonId() throws Exception{
        String moduleName = "Test Module " + UUID.randomUUID().getLeastSignificantBits();
        String basicSettingXml = createBasicSetting(moduleName);
        TextMessage responseMessage = sendStringToConfigAndReturnResponse(basicSettingXml, ConfigModuleMethod.PUSH);

        PushSettingsResponse response = JAXBMarshaller.unmarshallTextMessage(responseMessage, PushSettingsResponse.class);

        SettingType setting = null;
        for (SettingType s : response.getSettings()) {
            if(!s.isGlobal()){
                setting = s;
            }
        }
        setting.setId(null);
        String xml = ModuleRequestMapper.toResetSettingRequest(setting);

        TextMessage message = sendStringToConfigAndReturnResponse(xml, ConfigModuleMethod.RESET);
        SingleSettingResponse ssResponse = JAXBMarshaller.unmarshallTextMessage(message, SingleSettingResponse.class);

        assertNotNull(ssResponse.getSetting());

        String listXml = ModuleRequestMapper.toListSettingsRequest(moduleName);
        TextMessage listMessage = sendStringToConfigAndReturnResponse(listXml, ConfigModuleMethod.LIST);

        SettingsListResponse listResponse = JAXBMarshaller.unmarshallTextMessage(listMessage, SettingsListResponse.class);

        assertEquals(14, listResponse.getSettings().size());    //created two, plus 13 global and then minus one

    }


    private String createBasicSetting(String moduleName) throws Exception{
        String username = moduleName + " test user";

        List<SettingType> settings = new ArrayList();
        SettingType setting = new SettingType();
        setting.setGlobal(false);
        setting.setModule(moduleName);
        setting.setKey(moduleName + " key 1");
        setting.setValue(moduleName + " value 1");
        setting.setDescription(moduleName + " description 1");
        settings.add(setting);

        setting = new SettingType();
        setting.setGlobal(false);
        setting.setModule(moduleName);
        setting.setKey(moduleName + " key 2");
        setting.setValue(moduleName + " value 2");
        setting.setDescription(moduleName + " description 2");
        settings.add(setting);

        String request = ModuleRequestMapper.toPushSettingsRequest(moduleName, settings, username);
        return request;
    }

    private TextMessage sendStringToConfigAndReturnResponse(String xml, ConfigModuleMethod method) throws Exception{
        String corrID = jmsHelper.sendConfigMessage(xml, method.value());
        return (TextMessage) jmsHelper.listenForResponse(corrID);
    }
}
