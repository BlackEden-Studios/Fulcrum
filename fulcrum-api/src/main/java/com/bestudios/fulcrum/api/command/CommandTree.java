package com.bestudios.fulcrum.api.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.bestudios.fulcrum.api.command.CommandUtils.DEFAULT_LOCALE;

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

  /** The root node of the command tree */
  private final CommandNode rootNode;
  /** The base permission required to execute any command in this tree */
  private final @Nullable String basePermission;
  /** The usage message for this command tree */
  private final String usage;

  /**
   * Creates a new CommandTree
   * @param permission   The base permission required to execute any command in this tree
   * @param usageMessage The usage message for this command tree
   */
  public CommandTree(@Nullable String permission, @Nullable String usageMessage) {
    this.basePermission = permission;
    this.usage = usageMessage != null ? usageMessage : "Nothing here yet. Try /plugin_name help.";
    this.rootNode = new CommandNode();
  }

  /**
   * Creates a new CommandTree with specified base permission and no usage message
   * @param permission The base permission required to execute any command in this tree
   */
  public CommandTree(@Nullable String permission) {
    this(permission, null);
  }

  /**
   * Creates a new CommandTree with no base permission or usage message
   */
  public CommandTree() {
    this(null, null);
  }

  /**
   * Registers a command with the specified path and action.
   * If the action is null, this command node will not execute any action.
   *
   * @param path   The command path, e.g., "command subcommand"
   * @param action The action to execute when the command is called
   */
  public CommandTree registerCommand(@NotNull String path, CommandAction action) {
    return registerCommand(path, action, null, false);
  }

  /**
   * Registers a command with the specified path, action and permission.
   * If the action is null, this command node will not execute any action.
   *
   * @param path       The command path, e.g., "command subcommand"
   * @param action     The action to execute when the command is called
   * @param permission The permission required to execute this command
   */
  public CommandTree registerCommand(@NotNull String path, CommandAction action, String permission) {
    return registerCommand(path, action, permission, false);
  }

  /**
   * Registers a command with the specified path, action, and player-only behavior.
   * If the action is null, this command node will not execute any action.
   *
   * @param path            The command path, e.g., "command subcommand"
   * @param action          The action to execute when the command is called
   * @param isPlayerCommand If true, this action will only be executed by players.
   */
  public CommandTree registerCommand(@NotNull String path, CommandAction action, boolean isPlayerCommand) {
    return registerCommand(path, action, null, isPlayerCommand);
  }

  /**
   * Registers a command with the specified path, action, and permission.
   * If the action is null, this command node will not execute any action.
   *
   * @param path            The command path, e.g., "command subcommand"
   * @param action          The action to execute when the command is called
   * @param permission      The permission required to execute this command
   * @param isPlayerCommand If true, this action will only be executed by players.
   */
  public CommandTree registerCommand(@NotNull String path, CommandAction action, String permission, boolean isPlayerCommand) {
    this.findTargetNode(path.split("\\s+"), true).node()
        .setAction(action, permission, isPlayerCommand);

    return this;
  }

  /**
   * Registers a player command with the specified path and action.
   * If the action is null, this command node will not execute any action.
   *
   * @param path   The command path, e.g., "command subcommand"
   * @param action The action to execute when the command is called
   */
  public CommandTree registerPlayerCommand(@NotNull String path, CommandAction action) {
    return registerCommand(path, action, null, true);
  }

  /**
   * Registers a player command with the specified path, action, and permission.
   * If the action is null, this command node will not execute any action.
   *
   * @param path       The command path, e.g., "command subcommand"
   * @param action     The action to execute when the command is called
   * @param permission The permission required to execute this command
   */
  public CommandTree registerPlayerCommand(@NotNull String path, CommandAction action, String permission) {
    return registerCommand(path, action, permission, true);
  }

  /**
   * Registers a custom tab completer for a command path.
   * This allows for custom tab completion logic for specific commands.
   *
   * @param path      The command path, e.g., "command subcommand"
   * @param completer The function to handle tab completion for this command node
   */
  public CommandTree registerTabCompleter(String path, TabCompleteFunction completer) {
    this.findTargetNode(path.split("\\s+"), true).node()
        .setTabCompleter(completer);

    return this;
  }

  /**
   * Registers a custom tab completer for a command path.
   * This allows for custom tab completion logic for specific commands.
   *
   * @param path       The command path, e.g., "command subcommand"
   * @param completer  The function to handle tab completion for this command node
   * @param permission The permission required to execute this command
   */
  public CommandTree registerTabCompleter(String path, TabCompleteFunction completer, String permission) {
    return registerTabCompleter(path, completer);
  }

  /**
   * Registers a command from a {@link CommandWrapper}.
   * This is a convenience method for registering a command with the same path, action, and permission as the wrapper.
   *
   * @param command The command wrapper to register
   */
  public CommandTree registerFromCommandWrapper(@NotNull CommandWrapper command) {
    registerCommand(command.path(), command.action(), command.permission(), command.isPlayerCommand());
    registerTabCompleter(command.path(), command.tabCompleter(), command.permission());

    return this;
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
      sender.sendMessage(usage);
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
    return CommandUtils.filterCompletions(completions, partial);
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
      context.sender().sendMessage("Unknown subcommand. " + usage);
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
   * Functional interface for custom tab completion logic.
   */
  @FunctionalInterface
  public interface TabCompleteFunction {
    List<String> complete(CommandContext context);
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
     * @param isPlayerCommand   If true, this action will only be executed by players.
     */
    @Contract(mutates = "this")
    private void setAction(CommandAction actionToSet, String permissionToCheck, boolean isPlayerCommand) {
      this.action = isPlayerCommand ?
                    context -> {
                      if (!context.isPlayer()) {
                        context.sender().sendMessage("This command can only be used by players.");
                        return true;
                      }
                      return actionToSet.execute(context);
                    } : actionToSet;
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

}

