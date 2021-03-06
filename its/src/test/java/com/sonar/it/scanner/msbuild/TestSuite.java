/*
 * Scanner for MSBuild :: Integration Tests
 * Copyright (C) 2016-2016 SonarSource SA
 * mailto:contact AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.sonar.it.scanner.msbuild;

import java.nio.file.Path;

import org.junit.ClassRule;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import com.sonar.orchestrator.Orchestrator;
import com.sonar.orchestrator.config.Configuration;
import com.sonar.orchestrator.junit.SingleStartExternalResource;
import com.sonar.orchestrator.locator.FileLocation;
import com.sonar.orchestrator.locator.PluginLocation;


/**
 * csharpPlugin.version: csharp plugin to modify (installing scanner payload) and use. If not specified, uses 5.1. 
 * vbnetPlugin.version: vbnet plugin to use. It not specified, it fails
 * scannerForMSBuild.version: scanner to use. If not specified, uses the one built in ../
 * scannerForMSBuildPayload.version: scanner to embed in the csharp plugin. If not specified, uses the one built in ../
 * sonar.runtimeVersion: SQ to use
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
  ScannerMSBuildTest.class,
  CustomRoslynAnalyzerTest.class
})

public class TestSuite {
  public static Orchestrator ORCHESTRATOR;

  @ClassRule
  public static TemporaryFolder temp = new TemporaryFolder();

  @ClassRule
  public static SingleStartExternalResource resource = new SingleStartExternalResource() {
    @Override
    protected void beforeAll() {
      
      Path modifiedCs = TestUtils.prepareCSharpPlugin(temp);
      Path customRoslyn = TestUtils.getCustomRoslynPlugin();
      ORCHESTRATOR = Orchestrator.builderEnv()
        .addPlugin(FileLocation.of(modifiedCs.toFile()))
        .addPlugin(FileLocation.of(customRoslyn.toFile()))
        .addPlugin(PluginLocation.of("com.sonarsource.vbnet", "sonar-vbnet-plugin", getVBNetVersion()))
        .addPlugin("fxcop")
        .activateLicense("vbnet")
        .build();
      ORCHESTRATOR.start();
    }

    @Override
    protected void afterAll() {
      ORCHESTRATOR.stop();
    }
  };
  
  public static String getVBNetVersion() {
    Configuration configuration = Orchestrator.builderEnv().build().getConfiguration();
    String version = configuration.getString("vbnetPlugin.version");
    if(version != null) {
      return version;
    }
    
    throw new IllegalStateException("Unspecified version of VB.NET plugin. Define 'vbnetPlugin.version'");
  }
  
  public static String getCSharpVersion() {
    Configuration configuration = Orchestrator.builderEnv().build().getConfiguration();
    String version = configuration.getString("csharpPlugin.version");
    if(version != null) {
      return version;
    }
    
    throw new IllegalStateException("Unspecified version of C# plugin. Define 'csharpPlugin.version'");
  }
}
