testrail-jenkins-plugin
=======================

Forked from  https://github.com/simplymeasured/testrail-jenkins-plugin
Integrate test results from Jenkins into TestRail.
Upload your junit test results to TestRail after every run.
Each Jenkins build becomes test run.
Each testsuite becomes test group.

Important notice!
-----------------
This plugin is now maintained by Jenkins community   here https://github.com/jenkinsci/testrail-plugin 

Please **do not use the code from this repo**. 

If you have any questions, bugreports or pull requests please refer to the link above.

This fork changelog
---------------
- fixed validation issues
- added milestone support
- fixed junit files parsing
- added nested <testsuite> tags support in junit
- added dropdown lists to select projects, suites and milestones

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



License
-------
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
