package sample;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;

/**
 * Created by cmlanche on 2017/6/1.
 */
public class EditColumn extends StackPane {

    private EditColumnCallback callback;

    private Label label;
    private TextField textField;

    private StringProperty title;
    private BooleanProperty editing;
    private BooleanProperty editable;
    private BooleanProperty textFieldFocus;
    private TableColumn tableColumn;

    public EditColumn(TableColumn tableColumn) {
        super();
        this.tableColumn = tableColumn;
        this.init();
    }

    private void init() {
        this.setMaxWidth(120);
        this.setAlignment(Pos.CENTER);
        this.setStyle("-fx-background-color: #D9D9D9;");
        label = new Label();
        label.setStyle("-fx-font-size: 12px; -fx-text-fill: #333333;");
        textField = new TextField();
        textField.getStyleClass().add("tablefx-header-editor");
        label.textProperty().bindBidirectional(titleProperty());
        textField.textProperty().bindBidirectional(titleProperty());
        this.getChildren().addAll(label, textField);
        editingProperty().addListener((observable, oldValue, newValue) -> {
            if (isEditable()) {
                if (newValue) {
                    label.setVisible(false);
                    textField.setVisible(true);
                    textField.setFocusTraversable(true);
                    textField.requestFocus();
                } else {
                    label.setVisible(true);
                    textField.setVisible(false);
                }
            }
        });
        this.setStyle("-fx-background-color: transparent;");
        textField.setOnKeyPressed(event -> {
            if (isEditing()) {
                if (event.getCode() == KeyCode.ENTER) {
                    event.consume(); // 放置对tabview的其他列产生影响，不让消息透传
                    setEditing(false);
                    // 提交编辑
                    if (callback != null) {
                        callback.editCommit(tableColumn, textField.getText());
                    }
                } else if (event.getCode() == KeyCode.ESCAPE) {
                    setEditing(false);
                    // 取消编辑
                    if (callback != null) {
                        callback.cancelEdit();
                    }
                } else if (event.getCode() == KeyCode.TAB) {
                    setEditing(false);
                }
            }
        });
        textFieldFocusProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                textField.setFocusTraversable(true);
                textField.requestFocus();
            }
        });
        setEditing(false);
    }

    public String getTitle() {
        return titleProperty().get();
    }

    public StringProperty titleProperty() {
        if (title == null) {
            title = new SimpleStringProperty();
        }
        return title;
    }

    public void setTitle(String title) {
        this.titleProperty().set(title);
    }

    public boolean isEditing() {
        return editingProperty().get();
    }

    public BooleanProperty editingProperty() {
        if (editing == null) {
            editing = new SimpleBooleanProperty(true);
        }
        return editing;
    }

    public void setEditing(boolean editing) {
        this.editingProperty().set(editing);
    }

    public void setEditCallback(EditColumnCallback callback) {
        this.callback = callback;
    }

    public boolean isEditable() {
        return editableProperty().get();
    }

    public BooleanProperty editableProperty() {
        if (editable == null) {
            editable = new SimpleBooleanProperty(true);
        }
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editableProperty().set(editable);
    }

    public boolean isTextFieldFocus() {
        return textFieldFocusProperty().get();
    }

    public BooleanProperty textFieldFocusProperty() {
        if (textFieldFocus == null) {
            textFieldFocus = new SimpleBooleanProperty();
        }
        return textFieldFocus;
    }

    public void setTextFieldFocus(boolean textFieldFocus) {
        this.textFieldFocusProperty().set(textFieldFocus);
    }
}
