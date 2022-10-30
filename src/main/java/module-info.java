module ru.kryu.kchat.kchatservergui {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens ru.kryu.kchat.kchatservergui to javafx.fxml;
    exports ru.kryu.kchat.kchatservergui;
}