package commands;

import managers.AuthManager;
import managers.CollectionManager;
import managers.CommandManager;
import managers.DatabaseManager;

public class RegisterCommands {

    public static void register(CommandManager commandManager, CollectionManager collectionManager,
                                DatabaseManager db, AuthManager authManager) {
        commandManager.addCommand(new ExecuteScript());
        commandManager.addCommand(new Help());
        commandManager.addCommand(new Info(collectionManager));
        commandManager.addCommand(new Show(collectionManager));
        commandManager.addCommand(new Insert(collectionManager, db));
        commandManager.addCommand(new Update(collectionManager, db));
        commandManager.addCommand(new RemoveKey(collectionManager, db));
        commandManager.addCommand(new Clear(collectionManager, db));
        commandManager.addCommand(new RemoveLower(collectionManager, db));
        commandManager.addCommand(new History(commandManager));
        commandManager.addCommand(new RemoveGreaterKey(collectionManager, db));
        commandManager.addCommand(new RemoveAllByMinutesOfWaiting(collectionManager, db));
        commandManager.addCommand(new PrintAscending(collectionManager));
        commandManager.addCommand(new PrintFieldAscendingImpactSpeed(collectionManager));
        commandManager.addCommand(new Register(authManager));
        commandManager.addCommand(new Login(authManager));
    }
}
