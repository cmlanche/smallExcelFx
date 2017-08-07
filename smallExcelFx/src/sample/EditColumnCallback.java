package sample;

import javafx.scene.control.TableColumn;

/**
 * Created by cmlanche on 2017/6/1.
 */
public interface EditColumnCallback {

    /**
     * 开始编辑
     */
    void startEdit();

    /**
     * 提交编辑
     */
    void editCommit(TableColumn tableColumn, String newValue);

    /**
     * 取消编辑
     */
    void cancelEdit();
}
