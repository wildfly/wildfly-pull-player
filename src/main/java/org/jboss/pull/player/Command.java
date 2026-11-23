package org.jboss.pull.player;

public enum Command {

    HELP("/help", "Display this help text."),
    OK_TO_TEST ("/ok-to-test", "Add the user to the approved list of testers and run tests."),
    RETEST("/retest", "Rerun all tests.");

    private final String command;
    private final String description;

    Command(final String command, final String description) {
        this.command = command;
        this.description = description;
    }
    public final String getCommand() {
        return command;
    }
    public final String getDescription() {
        return description;
    }
}
