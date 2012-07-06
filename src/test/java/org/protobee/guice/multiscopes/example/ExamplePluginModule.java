/*******************************************************************************
 * Copyright (c) 2012, Daniel Murphy and Deanna Surma
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met:
 *   * Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other materials provided with
 * the distribution.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
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
