package org.protobee.guice;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({IncorrectUsesTests.class, ScopingTests.class})
public class MultiscopeTestSuite {}
