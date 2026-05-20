package gui.controllers;

import gui.i18n.LocaleManager;
import gui.util.LocalizedFormatter;
import network.CollectionInfo;
import network.CommandInfo;
import network.Response;

import java.util.List;
import java.util.stream.Collectors;

final class CommandResponseFormatter {
    private CommandResponseFormatter() {
    }

    static String formatInfo(Response response) {
        if (response.getPayload() instanceof CollectionInfo info) {
            return LocaleManager.get().tr("info.type", info.getType()) + "\n" +
                    LocaleManager.get().tr("info.initializationDate",
                            LocalizedFormatter.formatLocalDate(info.getCreationDate())) + "\n" +
                    LocaleManager.get().tr("info.elementsCount",
                            LocalizedFormatter.formatInteger(info.getSize()));
        }
        return response.getMessage();
    }

    static String formatHelp(Response response) {
        if (response.getPayload() instanceof List<?> payload) {
            List<CommandInfo> commands = payload.stream()
                    .filter(CommandInfo.class::isInstance)
                    .map(CommandInfo.class::cast)
                    .collect(Collectors.toList());
            if (!commands.isEmpty()) {
                String body = commands.stream()
                        .map(command -> command.getUsage() + " : " +
                                LocaleManager.get().trOrDefault(command.getDescriptionKey(),
                                        command.getFallbackDescription()))
                        .collect(Collectors.joining("\n"));
                return LocaleManager.get().tr("help.commands") + "\n" + body;
            }
        }
        return response.getMessage();
    }

    static String formatHistory(Response response) {
        if (response.getPayload() instanceof List<?> payload) {
            List<String> commands = payload.stream()
                    .filter(String.class::isInstance)
                    .map(String.class::cast)
                    .collect(Collectors.toList());
            if (commands.isEmpty()) {
                return LocaleManager.get().tr("history.empty");
            }
            return LocaleManager.get().tr("history.commands") + "\n" +
                    String.join("\n", commands);
        }
        return response.getMessage();
    }
}
