package Utilities;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;

public class MaximumRestrictedTextFieldListener implements ChangeListener<String> {

    private TextInputControl field;
    private int restriction;

    public MaximumRestrictedTextFieldListener(TextInputControl field, int restriction) {
        this.field = field;
        this.restriction = restriction;
    }

    @Override
    public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
        if (newValue.length() > restriction)
            field.setText(oldValue);
    }
}
