package org.protobee.guice.test.internal;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({UnboundedMultiscopeTests.class, BoundedTests.class})
public class InternalTestSuite {}
