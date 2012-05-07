package org.protobee.guice.example.scopedClasses;

import org.protobee.guice.example.scopes.BattlestarScope;

@BattlestarScope
public class CommandDeck {
  private String name;
  private int capacity;
  
  public String getName() {
    return name;
  }
  
  public int getCapacity() {
    return capacity;
  }
  
  public void setCapacity(int capacity) {
    this.capacity = capacity;
  }
  
  public void setName(String name) {
    this.name = name;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + capacity;
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    CommandDeck other = (CommandDeck) obj;
    if (capacity != other.capacity) return false;
    if (name == null) {
      if (other.name != null) return false;
    } else if (!name.equals(other.name)) return false;
    return true;
  }
}
