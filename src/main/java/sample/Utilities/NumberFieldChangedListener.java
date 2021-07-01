package sample.Utilities;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TextField;

public class NumberFieldChangedListener implements ChangeListener<String> {

    public static enum Type {INT, DOUBLE};

    private TextField field;
    private Type type;

    public NumberFieldChangedListener(TextField field, Type type) {
        this.field = field;
        this.type = type;
    }

    @Override
    public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
        if(newValue.isEmpty())
            return;
        if (type == Type.DOUBLE) {
            try {
                if (newValue.contains("-"))
                    throw new NumberFormatException();
                Double.parseDouble(newValue);
            } catch (NumberFormatException e) {
                field.setText(oldValue);
            }
        } else {
            try {
                if (newValue.contains("-"))
                    throw new NumberFormatException();
                Integer.parseInt(newValue);
            } catch (NumberFormatException e) {
                field.setText(oldValue);
            }
        }
    }
}