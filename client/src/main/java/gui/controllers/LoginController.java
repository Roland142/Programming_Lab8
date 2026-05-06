package gui.controllers;

import gui.App;
import gui.Session;
import gui.i18n.LocaleManager;
import gui.i18n.Localizer;
import gui.net.LoginService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import network.Response;

import java.util.Locale;

public class LoginController {

    @FXML private Label appTitleLabel;
    @FXML private Label appSubtitleLabel;
    @FXML private Label languageLabel;
    @FXML private ComboBox<Locale> languageBox;
    @FXML private Label loginTitleLabel;
    @FXML private Label usernameLabel;
    @FXML private TextField usernameField;
    @FXML private Label passwordLabel;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Button registerButton;
    @FXML private HBox errorBox;
    @FXML private Label errorLabel;

    private final LoginService loginService = new LoginService(Session.get().gateway());

    @FXML
    public void initialize() {
        Localizer.bind(appTitleLabel.textProperty(), "app.title");
        Localizer.bind(appSubtitleLabel.textProperty(), "app.subtitle");
        Localizer.bind(languageLabel.textProperty(), "common.language");
        Localizer.bind(loginTitleLabel.textProperty(), "login.title");
        Localizer.bind(usernameLabel.textProperty(), "login.username");
        Localizer.bind(passwordLabel.textProperty(), "login.password");
        Localizer.bind(loginButton.textProperty(), "login.submit");
        Localizer.bind(registerButton.textProperty(), "login.register");
        usernameField.promptTextProperty().bind(Localizer.binding("login.username.prompt"));
        passwordField.promptTextProperty().bind(Localizer.binding("login.password.prompt"));

        languageBox.getItems().setAll(LocaleManager.SUPPORTED);
        languageBox.setValue(LocaleManager.get().getLocale());
        Callback<javafx.scene.control.ListView<Locale>, ListCell<Locale>> cellFactory = lv -> new ListCell<>() {
            @Override
            protected void updateItem(Locale item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : LocaleManager.get().displayName(item));
            }
        };
        languageBox.setCellFactory(cellFactory);
        languageBox.setButtonCell(cellFactory.call(null));
        languageBox.valueProperty().addListener((obs, prev, value) -> {
            if (value != null) {
                LocaleManager.get().setLocale(value);
                languageBox.setButtonCell(cellFactory.call(null));
            }
        });
        LocaleManager.get().localeProperty().addListener((obs, prev, value) ->
                languageBox.setButtonCell(cellFactory.call(null)));

        errorBox.visibleProperty().bind(errorLabel.textProperty().isNotEmpty());
        errorBox.managedProperty().bind(errorBox.visibleProperty());
    }

    @FXML
    private void onLogin() {
        String login = usernameField.getText().trim();
        String password = passwordField.getText();
        if (login.isEmpty() || password.isEmpty()) {
            errorLabel.setText(LocaleManager.get().tr("login.error.empty"));
            return;
        }
        clearError();
        loginButton.setDisable(true);
        registerButton.setDisable(true);
        runAuthAttempt(loginService.login(login, password), "login.error.invalid", "login-task");
    }

    @FXML
    private void onRegister() {
        String login = usernameField.getText().trim();
        String password = passwordField.getText();
        if (login.isEmpty() || password.isEmpty()) {
            errorLabel.setText(LocaleManager.get().tr("login.error.empty"));
            return;
        }
        clearError();
        loginButton.setDisable(true);
        registerButton.setDisable(true);
        runAuthAttempt(loginService.register(login, password), "login.error.taken", "register-task");
    }

    private void runAuthAttempt(LoginService.AuthAttempt attempt,
                                String fallbackErrorKey, String threadName) {
        attempt.task.setOnSucceeded(e -> {
            loginButton.setDisable(false);
            registerButton.setDisable(false);
            Response response = attempt.task.getValue();
            if (response.isSuccess()) {
                Session.get().context().setCredentials(attempt.login, attempt.passwordHash);
                openMainWindow();
            } else {
                errorLabel.setText(localizedFromServer(response.getMessage(), fallbackErrorKey));
            }
        });
        attempt.task.setOnFailed(e -> {
            loginButton.setDisable(false);
            registerButton.setDisable(false);
            errorLabel.setText(LocaleManager.get().tr("login.error.server"));
        });
        new Thread(attempt.task, threadName).start();
    }

    private void clearError() {
        errorLabel.setText("");
    }

    private String localizedFromServer(String serverMessage, String fallbackKey) {
        if (serverMessage == null || serverMessage.isBlank())
            return LocaleManager.get().tr(fallbackKey);
        String lower = serverMessage.toLowerCase();
        if (lower.contains("неверн")) return LocaleManager.get().tr("login.error.invalid");
        if (lower.contains("занят")) return LocaleManager.get().tr("login.error.taken");
        return serverMessage;
    }

    private void openMainWindow() {
        Platform.runLater(App::showMain);
    }
}
