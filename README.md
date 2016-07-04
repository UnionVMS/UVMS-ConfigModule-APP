# ConfigModule

####Module description/purpose

The Config module is the main repository for all application settings. It stores both global and module-specific settings, and allows access to them by the application, typically through a REST interface. Settings can be accessed all at once (as a catalog), only the ones associated with a certain module including global settings or individually.

Individual module settings are synchronized with the Config module when either one is deployed to the application server. Newly deployed modules will initiate the synchronization process. Alternatively, if the Config is deployed last, it will notify all modules by posting on a topic, whereupon each module will initiate its own synchronization process accordingly. The process is as follows (using the Audit module as an example):

The Audit module sends a pull request to Config, requesting all of its settings.
If the Audit module is registered in the Config module, its current settings are returned. Otherwise, the Audit module is requested to, and subsequently will, upload its local settings to the Config module, which registers them for future use.

For the remaining runtime, the modules will access their settings locally. If settings are updated in the Config module, they will be pushed to the corresponding module on the aforementioned topic, and stored. It is also possible for modules to push settings to the Config module on the message queue.

Another feature of the Config module is keeping track of the availability of other modules, by collecting the latest timestamp of pings repeatedly transmitted by the modules on the Config module’s input queue. A summary of the state of each module can be retrieved through the Config modules REST interface by the application.

Much of this functionality (including synchronization, receiving changes during runtime and transmitting ping messages) is implemented in a separate project called uvms-config, which can be added as a dependency to modules. It requires the module to implement two interfaces for sending and receiving messages, as well as an interface that acts as a source for customizing the internal module name and a list of setting keys specific to that module. It also contains the classes used to access to the module’s local settings store, to reduce code duplication across modules
