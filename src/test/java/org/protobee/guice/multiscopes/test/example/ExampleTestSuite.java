package org.protobee.guice.multiscopes.test.example;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({MultipleScopeExampleTests.class, SingleScopeExampleTests.class,
    BoundedScopeExampleTests.class})
public class ExampleTestSuite {}
