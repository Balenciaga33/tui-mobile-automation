package com.tui.automation.runner;

import org.junit.platform.suite.api.ConfigurationParametersResource;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

@Suite
@IncludeEngines("cucumber")
@SelectPackages("com.tui.automation.features")
@ConfigurationParametersResource("cucumber.properties")
public class CucumberTest {
}
