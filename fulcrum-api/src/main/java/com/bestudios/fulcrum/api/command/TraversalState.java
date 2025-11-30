package com.bestudios.fulcrum.api.command;

import java.util.ArrayList;
import java.util.List;

/**
 * State holder for command traversal.
 * <p>
 * This holds the state as we iterate through the arguments.
 */
class TraversalState {
  CommandTree.CommandNode node;
  List<String> processedArgs;
  List<String> remainingArgs;
  boolean pathBroken;

  TraversalState(CommandTree.CommandNode root) {
    this.node = root;
    this.processedArgs = new ArrayList<>();
    this.remainingArgs = new ArrayList<>();
    this.pathBroken = false;
  }
}
