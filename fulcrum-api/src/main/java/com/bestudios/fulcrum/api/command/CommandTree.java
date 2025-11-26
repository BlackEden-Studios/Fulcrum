package com.bestudios.fulcrum.api.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * A functional approach to handling nested Minecraft commands.
 * <p>
 * Supports command trees like: /plugin_name command subcommand subcommands...
 *
 * @author Bestialus
 * @version 1.0
 * @since   1.0
 * @see CommandWrapper
 */
public class CommandTree implements CommandExecutor, TabCompleter {
  
  /** The default locale for command tree messages */
  private static final Locale DEFAULT_LOCALE = Locale.US;

  /** The root node of the command tree */
  private final CommandNode rootNode;
  /** The base permission required to execute any command in this tree */
  private @Nullable String basePermission;
  /** The usage message for this command tree */
  private String usageMessage = "Nothing here yet. Try /plugin_name help.";

  /** Creates a new CommandTree */
  public CommandTree() {
    this.rootNode = new CommandNode();
  }

  /**
   * Registers a command with the specified path and action.
   * If the action is null, this command node will not execute any action.
   *
   * @param path   The command path, e.g., "command subcommand"
   * @param action The action to execute when the command is called
   */
  public void registerCommand(String path, CommandAction action) {
    registerCommand(path, action, null);
  }

  /**
   * Registers a command with the specified path, action, and permission.
   * If the action is null, this command node will not execute any action.
   *
   * @param path       The command path, e.g., "command subcommand"
   * @param action     The action to execute when the command is called
   * @param permission The permission required to execute this command
   */
  public void registerCommand(String path, CommandAction action, String permission) {
    this.findTargetNode(path.split("\\s+"), true).node.setAction(action, permission);
  }

  /**
   * Registers a player-specific command with the specified path and action.
   * If the action is null, this command node will not execute any action.
   *
   * @param path   The command path, e.g., "player command"
   * @param action The action to execute when the command is called by a player
   */
  public void registerPlayerCommand(String path, PlayerCommandAction action) {
    registerPlayerCommand(path, action, null);
  }

  /**
   * Registers a player-specific command with the specified path, action, and permission.
   * If the action is null, this command node will not execute any action.
   *
   * @param path       The command path, e.g., "player command"
   * @param action     The action to execute when the command is called by a player
   * @param permission The permission required to execute this command
   */
  public void registerPlayerCommand(String path, PlayerCommandAction action, String permission) {
    this.findTargetNode(path.split("\\s+"), true).node().setPlayerAction(action, permission);
  }

  /**
   * Registers a custom tab completer for a command path.
   * This allows for custom tab completion logic for specific commands.
   *
   * @param path      The command path, e.g., "command subcommand"
   * @param completer The function to handle tab completion for this command node
   */
  public void registerTabCompleter(String path, TabCompleteFunction completer) {
    this.findTargetNode(path.split("\\s+"), true).node().setTabCompleter(completer);
  }

  /**
   * Registers a custom tab completer for a command path.
   * This allows for custom tab completion logic for specific commands.
   *
   * @param path       The command path, e.g., "command subcommand"
   * @param completer  The function to handle tab completion for this command node
   * @param permission The permission required to execute this command
   */
  public void registerTabCompleter(String path, TabCompleteFunction completer, String permission) {
    registerTabCompleter(path, completer);
  }

  public void registerFromCommandWrapper(CommandWrapper commandWrapper) {
    if (commandWrapper.isPlayerCommand())
      registerPlayerCommand(commandWrapper.path(), commandWrapper.playerAction(), commandWrapper.permission());
    else
      registerCommand(commandWrapper.path(), commandWrapper.action(), commandWrapper.permission());
    registerTabCompleter(commandWrapper.path(), commandWrapper.tabCompleter(), commandWrapper.permission());
  }

  /**
   * Handles command execution.
   *
   * @param sender  Source of the command
   * @param command Command which was executed
   * @param label   Alias of the command which was used
   * @param args    Passed command arguments
   * @return true if the command was handled successfully, false otherwise
   */
  @Override
  public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                           @NotNull String label, @NotNull String[] args) {

    // 1. Base Checks
    if (basePermission != null && !sender.hasPermission(basePermission)) {
      sender.sendMessage(Component.text("You don't have permission to use this command!")
                                  .color(NamedTextColor.RED));
      return true;
    }

    if (args.length == 0) {
      sender.sendMessage(usageMessage);
      return true;
    }

    // 2. Traversal
    TraversalResult result = findTargetNode(args, false);

    // 3. Construct Context with the found arguments
    CommandContext context = new CommandContext(sender, command, result.processedArgs(), result.remainingArgs());

    // 4. Execution
    return executeNode(result.node(), context);
  }

  /**
   * Handles tab completion for commands.
   * This method provides suggestions based on the current command context and arguments.
   *
   * @param sender  The command sender
   * @param command The command being executed
   * @param alias   The alias used for the command
   * @param args    The arguments passed to the command
   * @return A list of possible completions for the current command context
   */
  @Override
  public @Nullable List<String> onTabComplete(
    @NotNull CommandSender sender,
    @NotNull Command command,
    @NotNull String alias,
    @NotNull String[] args
  ) {
    // 1. Check base permission, if any
    if (basePermission != null && !sender.hasPermission(basePermission))
      return Collections.emptyList();

    // 2. Traversal
    TraversalResult result = findTargetNode(args, false);

    // 2.1 Check if the node has a permission, if any
    if (result.node().hasPermission() && !sender.hasPermission(result.node().getPermission()))
      return Collections.emptyList();

    // 3. Get the completions for the current node
    CommandContext context = new CommandContext(sender, command, result.processedArgs(), result.remainingArgs());
    List<String> completions = result.node.complete(context);

    // 4. Get partial input for filtering
    String partial = result.remainingArgs.length > 0 ? result.remainingArgs[0] : "";

    // 5. Filter based on partial input
    return filterCompletions(completions, partial);
  }

  /**
   * Represents the result of a command traversal.
   * <p>
   * This is used to store the deepest node matching the arguments, along with the command context for execution.
   * @param node          The deepest node matching the arguments.
   * @param processedArgs The arguments that were processed up to the deepest node.
   * @param remainingArgs The remaining arguments after the deepest node was reached.
   */
  private record TraversalResult(CommandNode node, String[] processedArgs, String[] remainingArgs) {}

  /**
   * State holder for command traversal.
   * <p>
   * This holds the state as we iterate through the arguments.
   */
  private static class TraversalState {
    CommandNode currentNode;
    List<String> processedArgs;
    List<String> remainingArgs;
    boolean pathBroken;

    TraversalState(CommandNode root) {
      this.currentNode = root;
      this.processedArgs = new ArrayList<>();
      this.remainingArgs = new ArrayList<>();
      this.pathBroken = false;
    }
  }

  /**
   * Traverses the tree to find the deepest executable node matching the arguments.
   *
   * @param args                The arguments to traverse with.
   * @param createNewNodeIfNull If true, a new node will be created if none exists for the current argument.
   * @return The traversal result containing the deepest node and remaining arguments.
   */
  @Contract("_, _ -> new")
  private @NotNull TraversalResult findTargetNode(String @NotNull [] args, boolean createNewNodeIfNull) {
    // Start at root with empty subcommands
    TraversalState state = new TraversalState(rootNode);
    //
    for (String arg : args) {
      // If the path was already broken (we hit an argument that wasn't a subcommand),
      // we stop checking for children and treat everything else as parameters.
      if (state.pathBroken) {
        state.remainingArgs.add(arg);
        continue;
      }

      // Try to find the child
      CommandNode child = createNewNodeIfNull ? state.currentNode.getOrCreateChild(arg) :
                                                state.currentNode.getChild(arg);

      if (child != null) {
        // If child found: it becomes the current node for the next iteration
        state.currentNode = child;
        state.processedArgs.add(arg);
      } else {
        // If no child found: the path ends here.
        // This arg and all later subcommands are "remaining subcommands".
        state.pathBroken = true;
        state.remainingArgs.add(arg);
      }
    }

    return new TraversalResult(
            state.currentNode,
            state.processedArgs.toArray(String[]::new),
            state.remainingArgs.toArray(String[]::new)
    );
  }

  /**
   * Validates permission and executes the found node.
   */
  private boolean executeNode(CommandNode node, CommandContext context) {
    // Check action, if any
    if (!node.hasAction()) {
      context.sender().sendMessage("Unknown subcommand. " + usageMessage);
      return true;
    }
    // Check permission, if any
    if (node.hasPermission() && !context.sender().hasPermission(node.getPermission())) {
      context.sender().sendMessage("You don't have permission to use this command!");
      return true;
    }
    // Execute action, if any
    return node.execute(context);
  }


  /**
   * Functional interface for command actions
   */
  @FunctionalInterface
  public interface CommandAction {
    boolean execute(CommandContext context);
  }

  /**
   * Functional interface for player-specific command actions
   */
  @FunctionalInterface
  public interface PlayerCommandAction {
    boolean execute(Player player, CommandContext context);
  }

  /**
   * Functional interface for custom tab completion logic.
   */
  @FunctionalInterface
  public interface TabCompleteFunction {
    List<String> complete(CommandContext context);
  }

  /**
   * Functional interface for registering a batch of commands with their respective actions.
   * @param <T> The type of the action to register
   */
  @FunctionalInterface
  private interface BatchRegistrar<T> {
    void register(String path, T action, String permission);
  }

  /**
   * Represents a node in the command tree.
   * Each node can have child nodes, an action to execute, a permission required to execute the action,
   * and a tab completer for custom tab completion logic.
   */
  private static class CommandNode {

    /** The children of this command node, mapping subcommand names to their respective CommandNodes */
    private final Map<String, CommandNode> children = new ConcurrentHashMap<>();
    /** The action to execute when this command node is reached */
    private CommandAction action;
    /** The tab completer function for this command node, used for custom tab completion logic */
    private TabCompleteFunction tabCompleter;
    /** The permission required to execute this command node's action */
    private String permission;

    /**
     * Gets the child command node with the specified name.
     * The name is case-insensitive.
     *
     * @param name The name of the child command node to retrieve
     * @return The CommandNode for the specified child, or null if it does not exist
     */
    private CommandNode getChild(String name) {
      return children.get(name.toLowerCase(DEFAULT_LOCALE));
    }

    /**
     * Gets or creates a child command node with the specified name.
     * If the child does not exist, it will be created.
     *
     * @param name The name of the child command node to retrieve or create
     * @return The CommandNode for the specified child
     */
    private CommandNode getOrCreateChild(String name) {
      return children.computeIfAbsent(name.toLowerCase(DEFAULT_LOCALE), k -> new CommandNode());
    }

    /**
     * Gets the names of all child command nodes.
     * This is useful for tab completion or listing available subcommands.
     *
     * @return A set of child command names
     */
    @Contract(pure = true)
    private @NotNull Set<String> getChildNames() {
      return children.keySet();
    }

    /**
     * Checks if this command node has an action to execute.
     * This is used to determine if the command can be executed when reached.
     *
     * @return true if this command node has an action, false otherwise
     */
    @Contract(pure = true)
    private boolean hasAction() {
      return action != null;
    }

    /**
     * Sets the action for this command node with an optional permission.
     * If the action is null, this node will not execute any action.
     *
     * @param actionToSet       The action to execute when this command node is reached
     * @param permissionToCheck The permission required to execute this action, or null if no permission is required
     */
    @Contract(mutates = "this")
    private void setAction(CommandAction actionToSet, String permissionToCheck) {
      this.action = actionToSet;
      this.permission = permissionToCheck;
    }

    /**
     * Sets the player-specific action for this command node with an optional permission.
     * If the action is null, this node will not execute any action.
     *
     * @param playerAction      The action to execute when this command node is reached by a player
     * @param permissionToCheck The permission required to execute this action, or null if no permission is required
     */
    @Contract(mutates = "this")
    private void setPlayerAction(PlayerCommandAction playerAction, String permissionToCheck) {
      this.action = context -> {
        if (!context.isPlayer()) {
          context.sender().sendMessage("This command can only be used by players.");
          return true;
        }
        return playerAction.execute(context.getPlayer(), context);
      };
      this.permission = permissionToCheck;
    }

    /**
     * Checks if this command node has a tab completer.
     * This is used to determine if the command can provide custom tab completion.
     *
     * @return true if this command node has a tab completer, false otherwise
     */
    @Contract(pure = true)
    private boolean hasTabCompleter() {
      return tabCompleter != null;
    }

    /**
     * Sets the tab completer for this command node.
     * If the completer is null, this node will not provide custom tab completion.
     *
     * @param completer The function to handle tab completion for this command node
     */
    @Contract(mutates = "this")
    private void setTabCompleter(TabCompleteFunction completer) {
      this.tabCompleter = completer;
    }

    /**
     * Checks if this command node has a specific permission required to execute its action.
     * This is used to enforce permissions when executing commands.
     *
     * @return true if this command node has a permission, false otherwise
     */
    @Contract(pure = true)
    private boolean hasPermission() {
      return permission != null;
    }

    /**
     * Gets the permission required to execute this command node's action.
     * This should only be called if hasPermission() returns true.
     *
     * @return The permission string required to execute this command node's action
     */
    @Contract(pure = true)
    private String getPermission() {
      return permission != null ? permission : "";
    }

    /**
     * Executes the action associated with this command node.
     * This should only be called if hasAction() returns true.
     *
     * @param context The command context containing sender, command, and arguments
     * @return true if the action was executed successfully, false otherwise
     */
    private boolean execute(CommandContext context) {
      return action != null && action.execute(context);
    }

    /**
     * Completes the command based on the current context.
     * If a tab completer is set, it will be used to provide custom completions.
     * Otherwise, it returns the names of child commands for tab completion.
     *
     * @param context The command context containing sender, command, and arguments
     * @return A list of strings representing possible completions for this command node
     */
    private List<String> complete(CommandContext context) {
      if (hasTabCompleter()) return tabCompleter.complete(context);
      // Default: return child command names
      return new ArrayList<>(getChildNames());
    }
  }

  /**
   * Builder for creating a CommandTree instance
   * <p>
   * Usage:
   * <pre>
   * new CommandTree.Builder("plugin.base", "Usage: /plugin command [subcommands]")
   *                   .command("command subcommand", context -> {
   *                      // Handle command logic.
   *                      return true;
   *                   })
   *                   .playerCommand("player command", (player, context) -> {
   *                      // Handle player-specific command logic.
   *                      return true;
   *                   })
   *                   .tabCompleter("command subcommand", context -> {
   *                      // Provide custom tab completion logic.
   *                      return List.of("option1", "option2");
   *                   })
   *                   .build();
   * </pre>
   */
  public static class Builder {
    /**
     * The CommandTree instance being built
     */
    private final CommandTree tree;

    /**
     * Creates a new Builder for a CommandTree instance
     */
    public Builder() {
      this.tree = new CommandTree();
    }

    /**
     * Register the base permission for all commands registered with this Builder instance.
     *
     * @param basePermission The base permission string to check
     * @return this Builder instance for chaining
     */
    public Builder basePermission(String basePermission) {
      tree.basePermission = basePermission;
      return this;
    }

    /**
     * Sets the usage message for all commands registered with this Builder instance.
     *
     * @param usageMessage The usage message to display
     * @return this Builder instance for chaining
     */
    public Builder usageMessage(String usageMessage) {
      tree.usageMessage = usageMessage;
      return this;
    }

    /**
     * Registers a command with the specified path and action.
     *
     * @param path   The command path, e.g., "command subcommand"
     * @param action The action to execute when the command is called
     * @return this Builder instance for chaining
     */
    public Builder command(String path, CommandAction action) {
      return command(path, action, null);
    }

    /**
     * Registers a command with the specified path, action, and permission.
     *
     * @param path       The command path, e.g., "command subcommand"
     * @param action     The action to execute when the command is called
     * @param permission The permission required to execute this command
     * @return this Builder instance for chaining
     */
    public Builder command(String path, CommandAction action, String permission) {
      tree.registerCommand(path, action, permission);
      return this;
    }

    /**
     * Registers multiple commands with their respective actions.
     *
     * @param commands A map of command paths to their actions
     * @return this Builder instance for chaining
     */
    public Builder commands(Map<String, CommandAction> commands) {
      return commands(commands, null);
    }

    /**
     * Registers multiple commands with their respective actions and a base permission.
     *
     * @param commands   A map of command paths to their actions
     * @param permission The permission required to execute these commands
     * @return this Builder instance for chaining
     */
    public Builder commands(Map<String, CommandAction> commands, String permission) {
      return registerBatch(commands, permission, tree::registerCommand);
    }

    /**
     * Registers a player-specific command with the specified path and action.
     *
     * @param path   The command path, e.g., "player command"
     * @param action The action to execute when the command is called by a player
     * @return this Builder instance for chaining
     */
    public Builder playerCommand(String path, PlayerCommandAction action) {
      return playerCommand(path, action, null);
    }

    /**
     * Registers a player-specific command with the specified path, action, and permission.
     *
     * @param path       The command path, e.g., "player command"
     * @param action     The action to execute when the command is called by a player
     * @param permission The permission required to execute this command
     * @return this Builder instance for chaining
     */
    public Builder playerCommand(String path, PlayerCommandAction action, String permission) {
      tree.registerPlayerCommand(path, action, permission);
      return this;
    }

    /**
     * Registers multiple player-specific commands with their respective actions.
     *
     * @param commands A map of command paths to their player-specific actions
     * @return this Builder instance for chaining
     */
    public Builder playerCommands(Map<String, PlayerCommandAction> commands) {
      return playerCommands(commands, null);
    }

    /**
     * Registers multiple player-specific commands with their respective actions and a base permission.
     *
     * @param commands   A map of command paths to their player-specific actions
     * @param permission The permission required to execute these commands
     * @return this Builder instance for chaining
     */
    public Builder playerCommands(Map<String, PlayerCommandAction> commands, String permission) {
      return registerBatch(commands, permission, tree::registerPlayerCommand);
    }

    /**
     * Registers a custom tab completer for a command path.
     *
     * @param path      The command path, e.g., "command subcommand"
     * @param completer The function to handle tab completion
     * @return this Builder instance for chaining
     */
    public Builder tabCompleter(String path, TabCompleteFunction completer) {
      tree.registerTabCompleter(path, completer);
      return this;
    }

    /**
     * Registers multiple custom tab completers for command paths.
     *
     * @param completers A map of command paths to their tab completion functions
     * @return this Builder instance for chaining
     */
    public Builder tabCompleters(Map<String, TabCompleteFunction> completers) {
      return registerBatch(completers, null, tree::registerTabCompleter);
    }

    /**
     * Registers a command wrapper with the specified path and action.
     * @param commandWrapper The command wrapper to register
     * @return this Builder instance for chaining
     */
    public Builder fromCommandWrapper(CommandWrapper commandWrapper) {
      if (commandWrapper.isPlayerCommand())
        tree.registerPlayerCommand(commandWrapper.path(), commandWrapper.playerAction(), commandWrapper.permission());
      else
        tree.registerCommand(commandWrapper.path(), commandWrapper.action(), commandWrapper.permission());
      tree.registerTabCompleter(commandWrapper.path(), commandWrapper.tabCompleter(), commandWrapper.permission());
      return this;
    }

    /**
     * Register a batch of commands with their respective actions.
     * @param map        A map of command paths to their actions
     * @param permission The permission required to execute these commands
     * @param registrar  The registrar to use to register the commands
     * @param <T>        The type of the action to register
     * @return this Builder instance for chaining
     */
    private <T> Builder registerBatch(Map<String, T> map, String permission, BatchRegistrar<T> registrar) {
      // No commands to register
      if (map == null || map.isEmpty()) return this;
      // Register each command
      for (Map.Entry<String, T> entry : map.entrySet())
        registrar.register(entry.getKey(), entry.getValue(), permission);
      return this;
    }

    /**
     * Builds the CommandTree instance with all registered commands and settings.
     *
     * @return The constructed CommandTree instance
     */
    public CommandTree build() {
      // Validate that the base permission is set
      if (tree.basePermission != null && tree.basePermission.isBlank())
        throw new IllegalStateException("Base permission cannot be blank or empty");
      // Validate that at least one command is registered
      if (tree.rootNode.children.isEmpty())
        throw new IllegalStateException("CommandTree must have at least one registered command");
      // Return the CommandTree instance
      return tree;
    }
  }

  /**
   * Utility method to filter a list of strings based on a partial match.
   * This is used for command tab completion to filter possibilities.
   *
   * @param possibilities The list of possible completions
   * @param partial       The partial string to match against
   * @return A filtered list of strings that start with the given partial string
   */
  public static List<String> filterCompletions(List<String> possibilities, String partial) {
    return possibilities.stream()
                        .filter(s -> s.toLowerCase(DEFAULT_LOCALE)
                                              .startsWith(partial.toLowerCase(DEFAULT_LOCALE)))
                        .collect(Collectors.toList());
  }

  /**
   * Utility method to get player completions for a specific argument index in the command context.
   * This is used for tab completion to suggest online players based on the partial input.
   *
   * @param context        The command context
   * @param playerArgIndex The index of the argument where player names are expected
   * @return A list of player names that match the partial input
   */
  public static List<String> playerTabCompletions(CommandContext context, int playerArgIndex) {
    // 1. Check if there are enough arguments to provide the player argument
    if (context.remainingArgs().length <= playerArgIndex) {
      String partial = context.remainingArgs().length > (playerArgIndex - 1) ?
                               context.remainingArgs()[(playerArgIndex - 1)] :
                               "";

      return Bukkit.getOnlinePlayers().stream()
                                      .map(Player::getName)
                                      .filter(name -> name.toLowerCase(DEFAULT_LOCALE)
                                                                  .startsWith(partial.toLowerCase(DEFAULT_LOCALE)))
                                      .collect(Collectors.toList());
    }
    return List.of();
  }

  /**
   * Utility method to get the target player from the command context.
   * If no player is specified, it defaults to the command sender if they are a player.
   * If the target player is not found or not online, it sends an error message.
   *
   * @param context        The command context
   * @param playerArgIndex The index of the argument where the player name is expected (starting at 1)
   * @return The target player or null if not found
   */
  @Nullable
  public static Player getTargetPlayer(CommandContext context, int playerArgIndex, boolean implicitSender) {
    // 1. Check if there are enough arguments to provide the player argument
    if (context.remainingArgs().length >= playerArgIndex)
      return Bukkit.getPlayer(context.remainingArgs()[playerArgIndex - 1]);
    // 2. Check if the sender implicitly provides the player argument
    if (context.isPlayer() && implicitSender) return context.getPlayer();
    // 3. No arguments and console sender: return null
    return null;
  }

  /**
   * Asynchronously retrieves a target OfflinePlayer.
   * <p>
   * Priority:
   * 1. If an argument exists at the index, look up that player (Async).
   * 2. If no argument exists and the sender is a player, return sender (Sync).
   * 3. Otherwise, return null.
   *
   * @param context        The command context
   * @param playerArgIndex The index of the argument (starting at 1)
   * @return A CompletableFuture containing the OfflinePlayer or null
   */
  public static CompletableFuture<OfflinePlayer> getTargetOfflinePlayerAsync(
          CommandContext context,
          int playerArgIndex,
          boolean implicitSender
  ) {
    // 1. Check if the user provided a specific argument
    if (context.remainingArgs().length >= playerArgIndex)
      // Run the lookup on a separate thread to avoid blocking the main server tick
      return CompletableFuture.supplyAsync(() -> {
        // This method might trigger a web request to Mojang
        return Bukkit.getOfflinePlayer(context.remainingArgs()[playerArgIndex - 1]);
      });
    // 2. Fallback: If no argument is provided, use the sender if they are a player
    if (context.isPlayer() && implicitSender) { return CompletableFuture.completedFuture(context.getPlayer()); }
    // 3. No argument and console sender: return null
    return CompletableFuture.completedFuture(null);
  }
}

