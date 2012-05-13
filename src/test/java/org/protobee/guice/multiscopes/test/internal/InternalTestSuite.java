package org.protobee.guice.multiscopes.test.internal;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({UnboundedTests.class, BoundedTests.class, DescoperTests.class})
public class InternalTestSuite {}
