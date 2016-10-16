# wlp-jee-starter-kit

This starter kit is created with http://liberty-starter.wasdev.developer.ibm.com/.

## TODO after initial clone

* Replace all `my-parent-groupid`, `my-artifact-id` and `my-groupid` in the project
* Edit or create `~/.m2/settings.xml`

  ```xml
  <?xml version="1.0" encoding="UTF-8"?>
  <settings
  	xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.1.0 http://maven.apache.org/xsd/settings-1.1.0.xsd"
  	xmlns="http://maven.apache.org/SETTINGS/1.1.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">

  	<profiles>
  		<profile>
  			<id>Bluemix Public Settings</id>
  			<activation>
  				<activeByDefault>true</activeByDefault>
  			</activation>

  			<properties>
  				<!-- The Cloud Foundry or Bluemix organization, username and password
  					can be entered here. -->
  				<cf.org>YOUR_BLUEMIX_ORG</cf.org>
  				<cf.username>YOUR_BLUEMIX_USER</cf.username>
  				<cf.password>YOUR_BLUEMIX_PASSWORD</cf.password>

  				<accept.features.license>SET TO "true" IF YOU ACCEPT THE LICENSE</accept.features.license>
  			</properties>
  		</profile>
  	</profiles>
  </settings>
  ```

* `mvn install` will compile, test, package and deploy the application to Bluemix.
