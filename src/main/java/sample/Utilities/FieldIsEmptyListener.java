package Utilities;

import com.jfoenix.controls.JFXTextField;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public class FieldIsEmptyListener implements ChangeListener<Boolean> {

    private JFXTextField field;

    public FieldIsEmptyListener(JFXTextField field) {this.field = field;}

    @Override
    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        if (!newValue)
            field.validate();
    }
}
