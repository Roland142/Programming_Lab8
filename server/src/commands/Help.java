package commands;

import network.Request;
import network.Response;

/** Команда help — возвращает справку по всем доступным командам. */
public class Help extends Command {

    public Help() {
        super("help");
    }

    @Override
    public Response execute(Request request) {
        String help = "Доступные команды:\n" +
                "help : вывести справку по доступным командам\n" +
                "info : вывести информацию о коллекции\n" +
                "show : вывести все элементы коллекции\n" +
                "insert {key} : добавить новый элемент с заданным ключом\n" +
                "update {id} : обновить элемент коллекции с заданным id\n" +
                "remove_key {key} : удалить элемент по ключу\n" +
                "clear : удалить все свои элементы\n" +
                "execute_script {file_name} : исполнить скрипт из файла\n" +
                "exit : завершить клиентское приложение\n" +
                "remove_lower : удалить все свои элементы, меньшие заданного\n" +
                "history : вывести последние 12 команд\n" +
                "remove_greater_key {key} : удалить свои элементы с ключом больше заданного\n" +
                "remove_all_by_minutes_of_waiting {minutes} : удалить свои элементы с заданным minutesOfWaiting\n" +
                "print_ascending : вывести элементы в порядке возрастания\n" +
                "print_field_ascending_impact_speed : вывести значения impactSpeed в порядке возрастания";
        return new Response(help, true);
    }
}
