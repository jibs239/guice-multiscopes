package org.protobee.guice.example.scopes;

import org.protobee.guice.Multiscope;

public final class ExampleScopes {
  public static final Multiscope BATTLESTAR = new Multiscope("BATTLESTAR",
      BattlestarScopeHolder.class);
  
  public static final Multiscope FIGHTER = new Multiscope("FIGHTER", FighterScopeHolder.class);
}
