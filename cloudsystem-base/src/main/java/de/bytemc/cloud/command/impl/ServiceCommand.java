package de.bytemc.cloud.command.impl;

import de.bytemc.cloud.Base;
import de.bytemc.cloud.api.CloudAPI;
import de.bytemc.cloud.api.command.CloudCommand;
import de.bytemc.cloud.api.logger.LogType;
import de.bytemc.cloud.api.services.IService;
import de.bytemc.cloud.api.services.utils.ServiceState;

import java.util.Arrays;
import java.util.List;

public final class ServiceCommand extends CloudCommand {

    public ServiceCommand() {
        super("service", "Manage services", "ser");
    }

    @Override
    public void execute(CloudAPI cloudAPI, String[] args) {
        final var logger = cloudAPI.getLogger();

        if (args.length == 1 && args[0].equalsIgnoreCase("list")) {
            cloudAPI.getServiceManager().getAllCachedServices().forEach(it -> logger
                .log("Name of service '§b" + it.getName() + "§7' (§7State of service '§b" + it.getServiceState().getName() + "§7' | Node: '" + it.getGroup().getNode() + "')"));
            return;
        } else if (args.length >= 1) {
            cloudAPI.getServiceManager().getService(args[0]).ifPresentOrElse(service -> {

                if (args.length == 2 && args[1].equalsIgnoreCase("stop")) {
                    if (service.getServiceState() == ServiceState.PREPARED || service.getServiceState() == ServiceState.STOPPING) {
                        logger.logMessage("This service ist not started or already in stopping state.", LogType.WARNING);
                        return;
                    }
                    service.stop();
                    logger.logMessage("The service '§b" + service.getName() + "§7' is now stopped.");
                } else if (args.length > 1 && args[1].equalsIgnoreCase("command")) {
                    final var stringBuilder = new StringBuilder();
                    for (int i = 2; i < args.length; i++) stringBuilder.append(args[i]).append(" ");
                    final var command = stringBuilder.toString();
                    service.executeCommand(command);
                    logger.logMessage("Executed command '" + command + "' on service " + service.getName());
                } else {
                    logger.logMessages("Service information:",
                        "Name: §b" + service.getName(),
                        "Id: §b" + service.getServiceId(),
                        "Group: §b" + service.getGroup().getName(),
                        "Host: §b" + service.getHostName(),
                        "Port: §b" + service.getPort());
                }

            }, () -> logger.logMessage("This service does not exists.", LogType.WARNING));
            return;
        }

        final var help = "§7Use following command: §b";
        logger.logMessages(
            help + "service list §7- List all available services.",
            help + "service (name) start §7- Starting a specific service that not exists.",
            help + "service (name) stop §7- Stopping a specific service that exists.",
            help + "service (name) §7- Prints information about the specific service.",
            help + "service (name) command (command) §7- Executes a command on a server.");
    }

    @Override
    public List<String> tabComplete(String[] arguments) {
        if (arguments.length == 1) {
            return Base.getInstance().getServiceManager().getAllCachedServices().stream().map(IService::getName).toList();
        } else if (arguments.length == 2) {
            return Arrays.asList("list", "start", "stop", "command");
        }
        return super.tabComplete(arguments);
    }
}
