package org.protobee.guice.multiscopes.example.scoped;

import org.protobee.guice.multiscopes.example.scopes.GalaxyScope;

@GalaxyScope
public class GalaxyProperties {
  private final long totalSolarMass;
  private final long radiusLightYears;
  
  public GalaxyProperties(long totalSolarMass, long radiusLightYears) {
    this.totalSolarMass = totalSolarMass;
    this.radiusLightYears = radiusLightYears;
  }
  
  public long getTotalSolarMass() {
    return totalSolarMass;
  }
  
  public long getRadiusLightYears() {
    return radiusLightYears;
  }
}
