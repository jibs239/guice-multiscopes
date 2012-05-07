package org.protobee.guice.example.scopedClasses;

import java.util.Set;

import org.protobee.guice.example.Fighter;
import org.protobee.guice.example.scopes.BattlestarScope;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

@BattlestarScope
public class BattlestarFighterRoster {
  public final Set<Fighter> fighters = Sets.newHashSet();

  public void addFighter(Fighter fighter) {
    fighters.add(fighter);
  }

  public Set<Fighter> getFighters() {
    return ImmutableSet.<Fighter>copyOf(fighters);
  }

  public void removeFighter(Fighter fighter) {
    fighters.remove(fighter);
  }
}
