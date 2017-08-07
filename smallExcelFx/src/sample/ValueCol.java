package sample;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;

import java.util.List;

/**
 * Created by cmlanche on 2017/5/31.
 * 一列数据
 */
public class ValueCol {

    private String name;

    private ListProperty<ValueCell> colValues;

    public ListProperty<ValueCell> colValuesProperty() {
        if (colValues == null) {
            colValues = new SimpleListProperty<>(FXCollections.observableArrayList());
        }
        return colValues;
    }

    public List<ValueCell> getColValues() {
        return colValuesProperty().get();
    }

    public void setColValues(List<ValueCell> colValues) {
        this.colValuesProperty().addAll(colValues);
    }
}
