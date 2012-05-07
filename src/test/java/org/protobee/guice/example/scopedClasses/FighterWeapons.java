package org.protobee.guice.example.scopedClasses;

import org.protobee.guice.example.scopes.FighterScope;

@FighterScope
public class FighterWeapons {

  private int damage;
  private String name;

  public int getDamage() {
    return damage;
  }

  public String getName() {
    return name;
  }
  
  public void setDamage(int damage) {
    this.damage = damage;
  }
  
  public void setName(String name) {
    this.name = name;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + damage;
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    FighterWeapons other = (FighterWeapons) obj;
    if (damage != other.damage) return false;
    if (name == null) {
      if (other.name != null) return false;
    } else if (!name.equals(other.name)) return false;
    return true;
  }
}
