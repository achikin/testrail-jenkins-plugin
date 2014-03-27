testrail-jenkins-plugin
=======================

Integrate test results from Jenkins into TestRail.

Build
-----
This is a Maven project. You'll need the following in your ~/.m2/settings.xml.

    <settings>
      <pluginGroups>
        <pluginGroup>org.jenkins-ci.tools</pluginGroup>
      </pluginGroups>
      <profiles>
        <profile>
          <id>jenkins</id>
          <activation>
            <activeByDefault>true</activeByDefault>
          </activation>
          <repositories>
            <repository>
              <id>repo.jenkins-ci.org</id>
              <url>http://repo.jenkins-ci.org/public/</url>
            </repository>
          </repositories>
          <pluginRepositories>
            <pluginRepository>
              <id>repo.jenkins-ci.org</id>
              <url>http://repo.jenkins-ci.org/public/</url>
            </pluginRepository>
          </pluginRepositories>
        </profile>
      </profiles>
      <mirrors>
        <mirror>
          <id>repo.jenkins-ci.org</id>
          <url>http://repo.jenkins-ci.org/public/</url>
          <mirrorOf>m.g.o-public</mirrorOf>
        </mirror>
      </mirrors>
    </settings>
    
To run on your development box you can just do

    mvn hpi:run
    
That will build and start a Jenkins instance running at http://localhost:8080/jenkins. It will have the plugin installed but not configured.


And to build a package to install on your production Jenkins box

    mvn clean package
        
That creates a .hpi file in the target directory. For more information about installing plugins, please see https://wiki.jenkins-ci.org/display/JENKINS/Plugins.



TODO: ADD LICENSE INFO HERE

