package sample;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Created by cmlanche on 2017/5/31.
 * 一项
 */
public class ValueCell {

    /**
     * 列名
     */
    private StringProperty name;
    /**
     * cell的值
     */
    private StringProperty value;

    public StringProperty nameProperty() {
        if (name == null) {
            name = new SimpleStringProperty();
        }
        return name;
    }

    public StringProperty valueProperty() {
        if (value == null) {
            value = new SimpleStringProperty();
        }
        return value;
    }

    public String getName() {
        return nameProperty().get();
    }

    public void setName(String name) {
        this.nameProperty().set(name);
    }

    public String getValue() {
        return valueProperty().get();
    }

    public void setValue(String value) {
        this.valueProperty().set(value);
    }

    @Override
    public String toString() {
        return "VarCell{" +
                "name=" + nameProperty().get() +
                ", value=" + valueProperty().get() +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        return true;
    }
}
