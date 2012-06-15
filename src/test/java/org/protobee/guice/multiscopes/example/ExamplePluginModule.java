package org.protobee.guice.multiscopes.example;

import org.protobee.guice.multiscopes.BoundedMultiscopeBinder;
import org.protobee.guice.multiscopes.BoundedMultiscopeBinder.PrescopeType;
import org.protobee.guice.multiscopes.Multiscopes;
import org.protobee.guice.multiscopes.example.scoped.GalaxyProperties;
import org.protobee.guice.multiscopes.example.scoped.Star;
import org.protobee.guice.multiscopes.example.scopes.AndromedaGalaxy;
import org.protobee.guice.multiscopes.example.scopes.Galaxy;
import org.protobee.guice.multiscopes.example.scopes.GalaxyScope;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;

public class ExamplePluginModule extends AbstractModule {

  @Override
  protected void configure() {
    BoundedMultiscopeBinder galaxyScopes =
      Multiscopes.newBoundedBinder(binder(), GalaxyScope.class, Galaxy.class);
  galaxyScopes.addInstance(AndromedaGalaxy.class);

  TypeLiteral<Star[]> starArrayType = new TypeLiteral<Star[]>() {};

  galaxyScopes
      .prescopeInstance(AndromedaGalaxy.class)
      .addInstanceObject(Key.get(starArrayType, AndromedaGalaxy.class))
      .addInstanceObject(Key.get(GalaxyProperties.class, AndromedaGalaxy.class),
          PrescopeType.EAGER);
  }
  
  @Provides
  @AndromedaGalaxy
  public GalaxyProperties getProperties() {
    return new GalaxyProperties(710000000000l, 33000l);
  }

  @Provides
  @AndromedaGalaxy
  public Star[] getStars() {
    // just a couple blank ones for example
    return new Star[] {new Star() {}, new Star() {}, new Star() {}};
  }
}
