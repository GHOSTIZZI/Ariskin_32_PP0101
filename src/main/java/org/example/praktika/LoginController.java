package org.example.praktika;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.io.IOException;

public class LoginController {

    @FXML private TextField username;
    @FXML private PasswordField password;
    @FXML private Label statusLabel;
    private final AuthDAO authDAO = new AuthDAO();


    private boolean isPasswordVisible = false;


    @FXML private PasswordField passwordField;
    @FXML private TextField passwordTextField;
    @FXML private Button togglePasswordBtn;

    @FXML
    private void togglePasswordVisibility() {
        if (isPasswordVisible) {

            passwordField.setText(passwordTextField.getText());
            passwordField.setVisible(true);
            passwordField.setManaged(true);

            passwordTextField.setVisible(false);
            passwordTextField.setManaged(false);
            togglePasswordBtn.setText("👁");
        } else {

            passwordTextField.setText(passwordField.getText());
            passwordTextField.setVisible(true);
            passwordTextField.setManaged(true);

            passwordField.setVisible(false);
            passwordField.setManaged(false);
            togglePasswordBtn.setText("🔒");
        }
        isPasswordVisible = !isPasswordVisible;
    }

    @FXML
    private void Connect(ActionEvent event) {
        String user = username.getText().trim();
        String pass = isPasswordVisible ? passwordTextField.getText().trim() : passwordField.getText().trim();

        if (user.isEmpty() || pass.isEmpty()) {
            statusLabel.setText("Введите логин и пароль!");
            return;
        }

        statusLabel.setText("Подключение...");

        authDAO.login(user, pass)
                .thenAccept(loggedInUser -> {
                    Platform.runLater(() -> {
                        if (loggedInUser != null) {
                            try {

                                FXMLLoader loader = new FXMLLoader(getClass().getResource("form1.fxml"));
                                Parent root = loader.load();


                                Form1 form1Controller = loader.getController();
                                if (form1Controller != null) {
                                    form1Controller.setCurrentUser(
                                            loggedInUser.getId(),
                                            loggedInUser.getUsername(),
                                            loggedInUser.getRole()
                                    );
                                } else {
                                    throw new IllegalStateException("Контроллер Form1 не был получен из FXML.");
                                }


                                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();


                                stage.setScene(new Scene(root));
                                stage.show();


                                Platform.runLater(() -> {
                                    stage.setMaximized(false);
                                    stage.setMaximized(true);
                                });

                            } catch (Exception e) {
                                statusLabel.setText("Ошибка загрузки главного интерфейса. Проверьте FXML и логи.");
                                e.printStackTrace();
                            }
                        } else {
                            statusLabel.setText("Неверный логин или пароль!");
                        }
                    });
                })
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        String errorMsg = (ex.getCause() != null) ? ex.getCause().getMessage() : ex.getMessage();
                        statusLabel.setText("Сетевая ошибка при входе: " + errorMsg);
                        ex.printStackTrace();
                    });
                    return null;
                });
    }

    @FXML
    private void regi(ActionEvent event) throws IOException {
        Parent registerRoot = FXMLLoader.load(getClass().getResource("Register.fxml"));
        username.getScene().setRoot(registerRoot);
    }
}