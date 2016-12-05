# ConfigModule

####Module description/purpose

The Config module is the main repository for all application settings. It stores both global and module-specific settings, and allows access to them by the application, typically through a REST interface. Settings can be accessed all at once (as a catalog), only the ones associated with a certain module including global settings or individually.

Individual module settings are synchronized with the Config module when either one is deployed to the application server. Newly deployed modules will initiate the synchronization process. Alternatively, if the Config is deployed last, it will notify all modules by posting on a topic, whereupon each module will initiate its own synchronization process accordingly. The process is as follows (using the Audit module as an example):

The Audit module sends a pull request to Config, requesting all of its settings.
If the Audit module is registered in the Config module, its current settings are returned. Otherwise, the Audit module is requested to, and subsequently will, upload its local settings to the Config module, which registers them for future use.

For the remaining runtime, the modules will access their settings locally. If settings are updated in the Config module, they will be pushed to the corresponding module on the aforementioned topic, and stored. It is also possible for modules to push settings to the Config module on the message queue.

Another feature of the Config module is keeping track of the availability of other modules, by collecting the latest timestamp of pings repeatedly transmitted by the modules on the Config module’s input queue. A summary of the state of each module can be retrieved through the Config modules REST interface by the application.

Much of this functionality (including synchronization, receiving changes during runtime and transmitting ping messages) is implemented in a separate project called uvms-config, which can be added as a dependency to modules. It requires the module to implement two interfaces for sending and receiving messages, as well as an interface that acts as a source for customizing the internal module name and a list of setting keys specific to that module. It also contains the classes used to access to the module’s local settings store, to reduce code duplication across modules.

## JMS Queue Dependencies
The jndi name example is taken from wildfly8.2 application server

|Name           |JNDI name example              |Description                       |
|---------------|-------------------------------|----------------------------------|
|UVMSConfigEvent|java:/jms/queue/UVMSConfigEvent|Config request listening queue    |
|UVMSConfig     |java:/jms/queue/UVMSConfig     |Response queue                    |
|ConfigStatus   |java:/jms/topic/ConfigStatus   |Topic for posting to other modules|
|UVMSAuditEvent |java:/jms/queue/UVMSAuditEvent |Request queue for audit events    |

## Datasources
The jndi name example is taken from wildfly8.2 application server

|Name       |JNDI name example                 |
|-----------|----------------------------------|
|uvms_config|java:jboss/datasources/uvms_config|

## Related Repositories
* https://github.com/UnionVMS/UVMS-ConfigModule-DB
* https://github.com/UnionVMS/UVMS-ConfigModule-MODEL
* https://github.com/UnionVMS/UVMS-UVMSConfigLibrary

## Config Module Integration

The configuration sync handshake, and application runtime interaction, is bundled in an external JAR, called uvms-config (https://github.com/UnionVMS/UVMS-UVMSConfigLibrary). A few steps are needed to integrate it 
with your module.

When the module is deployed, it will pull its parameters (settings) from the Config module. If the module is unknown to the Config module, a push request is returned, whereupon the module will push its current paramaters to Config.

If the Config module deploys last, in announces itself using the ConfigStatus topic, and the handshake as describes above follows.

Changes are only necessary in the module (APP) project.

### Maven Dependency
Source code is available in https://github.com/UnionVMS/UVMS-UVMSConfigLibrary
```xml
<dependency>
	<groupId>eu.europa.ec.fisheries.uvms</groupId>
	<artifactId>uvms-config</artifactId>
	<version>2.1.1version>
</dependency>
```

### JMS
Implement the ConfigMessageConsumer and ConfigMessageProducer interfaces. Preferably, the existing consumer/producer beans should be re-used.

The new methods basically call the common producer/consumer methods, and wrap any exception in a ConfigMessageException. The config queue can be mapped as a @Resource in the producer bean.

#### Tips
* Use the queue enum to differentiate the new queue.
* Do not forget to annotate the new methods with @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
* If the Config module is not deployed, the deployment of other modules will fail after JMS queue timeout, because a null response cannot be handled.

In the Consumer bean getMessage method, check the repsponse, and if null, throw an exception with message "\[ Timeout reached or message null in RulesResponseConsumerBean. ]".

Also, reduce the timeout to 10 seconds so that deploying does not take forever when Config module is missing.
   
### Implement ConfigHelper
It should provide the module name and a list of strings of all parameter keys relevant to the module.

### Tips
* Create this bean in the service project's *.bean package.
* Use MessageConstants to define the module name, as recognized by the Config module.

### Remove duplicate code stuff
   Remove the Parameter entity, as well as the ParameterService interface and bean. Switch to the ParameterService included in uvms-config where needed.

### Setup the context and database
In persistence.xml, make sure that the name of the persistence unit is module name, and that the Parameter entity is specified in a <class>-tag.
```xml
    <class>eu.europa.ec.fisheries.uvms.config.service.entity.Parameter</class>
```
Your module's datasource schema must have a table called parameter, with parameter ID ("key"), value and description columns.