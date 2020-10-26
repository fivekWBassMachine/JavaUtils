package com.github.fivekwbassmachine.jutils;

import java.util.HashMap;
import java.util.Set;

/**
 * This data type is a better format for command line arguments.
 *
 * <p>
 *     There are 3 type of parsable arguments:
 *     - Keys with only one letter which can occur onetime or repeated and don't have a value:
 *       -[key]
 *       -[key1][...][key n]
 *     - Keys with multiple letters which can only occur onetime and don't have a value:
 *       --[key]
 *     - Keys with multiple letters which can only occur onetime and have a value:
 *       --[key] [value]
 * </p>
 *
 * @author 5kWBassMachine
 * @version 1.0.0
 */
public class CommandLineArguments {

    private final HashMap<String, Argument> arguments;

    /**
     * parses command line arguments formatted in an array of Strings to a CommandLineArguments Object.
     *
     * @param args The arguments to parse.
     * @return The parsed arguments.
     *
     * @since 1.0.0
     */
    public static CommandLineArguments from(String[] args) {
        CommandLineArguments parsedArgs = new CommandLineArguments();

        if (args.length == 0) return parsedArgs;

        String currentKey = null;
        StringBuilder currentValues = null;

        for (int i = 0; i < args.length; i++) {
            if (args[i].startsWith("-")) {
                if (currentKey != null) {
                    parsedArgs.add(currentKey);
                    currentKey = null;
                }
                args[i] = args[i].substring(1);
                if (args[i].startsWith("-")) {
                    args[i] = args[i].substring(1);
                    currentKey = args[i];
                }
                else {
                    for (char key : args[i].toCharArray()) parsedArgs.add(key);
                }
            }
            else if (currentKey == null) {
                throw new IllegalArgumentException("Can't parse a value without a key");
            }
            else {
                if (i + 1 < args.length && args[i + 1].startsWith("-")) {
                    if (currentValues == null) {
                        parsedArgs.add(currentKey, args[i]);
                        currentKey = null;
                    }
                    else {
                        parsedArgs.add(currentKey, currentValues.toString());
                        currentValues = null;
                    }
                }
                else {
                    if (currentValues == null) {
                        currentValues = new StringBuilder();
                    }
                    else {
                        currentValues.append(" ");
                    }
                    currentValues.append(args[i]);
                }
            }
        }
        if (currentKey != null) {
            if (currentValues == null) {
                parsedArgs.add(currentKey);
            }
            else {
                parsedArgs.add(currentKey, currentValues.toString());
            }
        }

        return parsedArgs;
    }

    /**
     * @since 1.0.0
     */
    public CommandLineArguments() {
        this.arguments = new HashMap<>();
    }

    /**
     * returns the arguments as a String with the default title.
     *
     * <p>
     *     Default title: Arguments:\n
     *
     *     Bug: The method returns the arguments in no particular order.
     *     This could be fixed with using a {@link java.util.LinkedHashMap} for {@link CommandLineArguments#arguments} to keep the order.
     *     Actually, this shouldn't be required.
     * </p>
     *
     * @return The arguments.
     *
     * @since 1.0.0
     */
    @Override
    public String toString() {
        return this.toString("Arguments:\n");
    }
    /**
     * returns the arguments as a String with a custom title.
     *
     * <p>
     *     Bug: The method returns the arguments in no particular order.
     *     This could be fixed with using a {@link java.util.LinkedHashMap} for {@link CommandLineArguments#arguments} to keep the order.
     *     Actually, this shouldn't be required.
     * </p>
     *
     * @param title The title including line break.
     *
     * @return The arguments.
     *
     * @since 1.0.0
     */
    public String toString(String title) {
        StringBuilder sb = new StringBuilder();
        sb.append(title);
        Object[] keys = this.arguments.keySet().toArray();
        for (Object o : keys) {
            String key = (String) o;
            Argument arg = this.arguments.get(key);
            switch (arg.type) {
                case KEY:
                    sb.append(key.length() == 1 ? "  K: -" : "  K: --");
                    sb.append(key);
                    sb.append("\n");
                    break;
                case PAIR:
                    sb.append("  P: --");
                    sb.append(key);
                    sb.append(" ");
                    sb.append(arg.value);
                    sb.append("\n");
                    break;
                case NONE:
                default:
                    throw new IllegalStateException();
            }
        }
        return sb.toString();
    }

    /**
     * adds a key to the arguments.
     *
     * @param arg The key to add.
     * @return this
     *
     * @since 1.0.0
     */
    public CommandLineArguments add(char arg) {
        return this.add(String.valueOf(arg));
    }
    /**
     * adds a key to the arguments.
     *
     * @param arg The key to add.
     * @return this
     *
     * @since 1.0.0
     */
    public CommandLineArguments add(String arg) {
        this.arguments.put(arg, new Argument());
        return this;
    }
    /**
     * adds a key-value-pair to the arguments.
     *
     * @param arg The key to add.
     * @param value The value to add.
     * @return this
     *
     * @since 1.0.0
     */
    public CommandLineArguments add(String arg, String value) {
        this.arguments.put(arg, new Argument(value));
        return this;
    }

    /**
     * returns the value of the argument or null if it doesn't exist.
     *
     * @param arg The argument.
     * @return The value of the argument.
     *
     * @since 1.0.0
     */
    public String get(String arg) {
        Argument argument = this.arguments.get(arg);
        return argument == null ? null : argument.value;
    }

    /**
     * returns the value of the argument or the default value if it doesn't exist.
     *
     * @param arg The argument.
     * @param defaultValue The default value.
     * @return The value of the argument.
     *
     * @since 1.0.0
     */
    public String getOrDefault(String arg, String defaultValue) {
        String value;
        return (value = this.get(arg)) == null ? defaultValue : value;
    }

    /**
     * returns the {@link ArgumentType} of the argument.
     *
     * <p>
     *     Returns
     *     - {@link ArgumentType#KEY} When the argument is a key.
     *     - {@link ArgumentType#PAIR} When the argument is a key-value-pair.
     *     - {@link ArgumentType#NONE} When the argument does not exist.
     * </p>
     *
     * @param arg The argument.
     * @return The type of the argument.
     *
     * @since 1.0.0
     */
    public ArgumentType getType(String arg) {
        if (!this.exists(arg)) return ArgumentType.NONE;
        return this.arguments.get(arg).type;
    }
    /**
     * returns the {@link ArgumentType} of the argument, if it doesn't exist the one of the alternative argument.
     *
     * <p>
     *     Returns
     *     - {@link ArgumentType#KEY} When the argument is a key.
     *     - {@link ArgumentType#PAIR} When the argument is a key-value-pair.
     *     - {@link ArgumentType#NONE} When the argument does not exist.
     * </p>
     *
     * @param arg The argument.
     * @param altArg The alternative argument.
     * @return The type of the argument.
     *
     * @since 1.0.0
     */
    public ArgumentType getType(String arg, String altArg) {
        if (!this.exists(arg)) return this.getType(altArg);
        return this.arguments.get(arg).type;
    }

    /**
     * returns whether the argument exists or not.
     *
     * @param arg The argument to check.
     * @return True, when the argument exists.
     *
     * @since 1.0.0
     */
    public boolean exists(String arg) {
        return this.arguments.containsKey(arg);
    }
    /**
     * returns whether the argument or the alternative argument exists or not.
     *
     * @param arg The argument to check.
     * @param altArg The alternative argument to check.
     * @return True, when the argument exists.
     *
     * @since 1.0.0
     */
    public boolean exists(String arg, String altArg) {
        return this.arguments.containsKey(arg) || this.arguments.containsKey(altArg);
    }

    /**
     * returns all stored arguments.
     *
     * @see HashMap#keySet()
     *
     * @return All stored arguments.
     *
     * @since 1.0.0
     */
    public Set<String> keySet() {
        return this.arguments.keySet();
    }

    public enum ArgumentType {
        KEY,
        PAIR,
        NONE;

        public boolean isKey() {
            return this == KEY;
        }

        public boolean isPair() {
            return this == PAIR;
        }
    }

    private class Argument {

        private final ArgumentType type;
        private final String value;

        private Argument() {
            this.type = ArgumentType.KEY;
            this.value = null;
        }

        private Argument(String value) {
            this.type = ArgumentType.PAIR;
            this.value = value;
        }
    }
}
