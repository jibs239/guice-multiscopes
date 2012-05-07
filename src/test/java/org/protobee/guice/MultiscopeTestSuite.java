package org.protobee.guice;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({InternalTests.class, MultipleScopeExampleTests.class, SingleScopeExampleTests.class})
public class MultiscopeTestSuite {}
