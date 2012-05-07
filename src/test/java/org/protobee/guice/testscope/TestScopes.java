package org.protobee.guice.testscope;

import org.protobee.guice.Multiscope;

public final class TestScopes {
  public static final Multiscope DEPARTMENT = new Multiscope("DEPARTMENT", DepartmentScopeHolder.class);
}
