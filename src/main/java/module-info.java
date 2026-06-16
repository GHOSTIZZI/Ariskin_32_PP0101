module org.example.praktika {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;


    requires javafx.media;
    requires java.net.http;
    requires com.fasterxml.jackson.databind;

    requires java.desktop;
    requires java.sql;



    opens org.example.praktika to javafx.fxml;
    exports org.example.praktika;


}
