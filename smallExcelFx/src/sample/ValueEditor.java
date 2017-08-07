package sample;

import com.alibaba.fastjson.JSONObject;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by cmlanche on 2017/5/31.
 */
public class ValueEditor {

    public static final String default_col_name = "请输入变量名";
    public static final String default_cell_value = "请输入变量值";
    public static final String first_col_name = "first_col_name";

    private ValueJson valueJson;

    private ValueEditor() {
    }

    private static class VarManagerHolder {
        public static ValueEditor instance = new ValueEditor();
    }

    public static ValueEditor instance() {
        return VarManagerHolder.instance;
    }

    public void init(ValueJson valueJson) {
        this.valueJson = valueJson;
        if (valueJson != null) {
            if (valueJson.getItems() != null && valueJson.getItems().isEmpty()) {
                // add default row and column
                ValueCol col = new ValueCol();
                ValueCell cell = new ValueCell();
                cell.setName(default_col_name);
                cell.setValue(default_cell_value);
                col.getColValues().add(cell);
                this.valueJson.getItems().add(col);
            }
        }
    }

    /**
     * 获取所有列
     *
     * @return
     */
    public List<ValueCol> getCols() {
        List<ValueCol> colNames = new ArrayList<>();
        if (hasData()) {
            return valueJson.getItems();
        } else {
            return colNames;
        }
    }

    /**
     * 删除指定列
     *
     * @param colIndex 用户视角，第几列
     * @return
     */
    public void deleteCol(int colIndex) {
        if (hasData()) {
            for (int i = 0; i < valueJson.getItems().size(); i++) {
                if (i == colIndex - 1) {
                    valueJson.getItems().remove(i);
                    break;
                }
            }
        }
    }

    /**
     * 添加一列
     *
     * @param colIndex 对用户而言的索引，第几列
     * @param isLeft
     */
    public int addCol(int colIndex, boolean isLeft, ValueCol item) {
        if (valueJson != null) {
            if (valueJson.getItems() != null) {
                int newColIndex; // 新添加的列索引，这个索引是针对数据而言的
                if (valueJson.getItems().size() == 0) {
                    newColIndex = 0;
                } else {
                    if (isLeft) {
                        if (colIndex <= 1) {
                            newColIndex = 0;
                        } else {
                            newColIndex = colIndex - 1;
                        }
                    } else {
                        newColIndex = colIndex;
                    }
                }
                // 如果item为空，则视为添加新列
                if (item == null) {
                    int rowCount = getRowCount();
                    item = new ValueCol();
                    for (int i = 0; i < rowCount; i++) {
                        ValueCell cell = new ValueCell();
                        cell.setName(default_col_name);
                        cell.setValue("");
                        item.colValuesProperty().add(cell);
                    }
                }
                valueJson.getItems().add(newColIndex, item);
                return newColIndex + 1; // 因为newColIndex是针对数据的索引，这里返回针对表格是索引，需+1
            }
        }

        // todo 如果为空数据，则应该添加一行一列
        return 0;
    }

    /**
     * 添加新列
     *
     * @param colIndex
     * @param isLeft
     */
    public int addNewCol(int colIndex, boolean isLeft) {
        return this.addCol(colIndex, isLeft, null);
    }

    /**
     * 添加一行数据
     *
     * @param rowIndex
     * @param isUp
     * @return 返回此行索引
     */
    public int addRow(int rowIndex, boolean isUp) {
        if (valueJson != null) {
            if (valueJson.getItems() != null) {
                int colCount = getColCount();
                int newRowIndex;
                if (rowIndex < 0) {
                    newRowIndex = 0;
                } else {
                    if (isUp) {
                        newRowIndex = rowIndex;
                    } else {
                        newRowIndex = rowIndex + 1;
                    }
                }
                // 在每列的对应行上增加一个cell
                for (int i = 0; i < colCount; i++) {
                    ValueCol col = valueJson.getItems().get(i);
                    ValueCell cell = new ValueCell();
                    cell.setName(col.getColValues().get(0).getName());
                    cell.setValue("");
                    col.colValuesProperty().add(newRowIndex, cell);
                }

                return newRowIndex;
            }
        }

        // todo 以前为空表，添加一行和一列
        return 0;
    }

    /**
     * 删除行
     */
    public void deleteRow(int rowIndex) {
        int rowCount = getRowCount();
        if (rowIndex < 0 || rowIndex > rowCount) {
            // ignore
        } else {
            int colCount = getColCount();
            // 统一删除每列的对应行
            for (int i = 0; i < colCount; i++) {
                ValueCol col = valueJson.getItems().get(i);
                col.getColValues().remove(rowIndex);
            }
        }
    }

    /**
     * 获取数据源
     *
     * @return
     */
    public List<ObservableList<ValueCell>> getDataSource() {
        if (hasData()) {
            int rowCount = getRowCount();
            List<ObservableList<ValueCell>> datasource = new ArrayList<>();
            for (int i = 0; i < rowCount; i++) {
                ObservableList<ValueCell> row = FXCollections.observableArrayList();
                ValueCell nCell = new ValueCell();
                nCell.setName(first_col_name);
                nCell.setValue(String.valueOf(i + 1));
                row.add(nCell);
                for (int j = 0; j < valueJson.getItems().size(); j++) {
                    ValueCol col = valueJson.getItems().get(j);
                    row.add(col.getColValues().get(i));
                }
                datasource.add(row);
            }
            return datasource;
        }
        return null;
    }

    /**
     * 获取行数
     *
     * @return
     */
    public int getRowCount() {
        if (hasData()) {
            return valueJson.getItems().get(0).colValuesProperty().size();
        }
        return 0;
    }

    /**
     * 获取列数
     *
     * @return
     */
    public int getColCount() {
        if (hasData()) {
            return valueJson.getItems().size();
        }
        return 0;
    }

    /**
     * 保存数据
     */
    public void saveData() {
        File file = new File("./data.json");
        try {
            String json = JSONObject.toJSONString(valueJson);
            FileUtils.write(file, json, "utf-8");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 编辑某项的值
     *
     * @param rowIndex >=0
     * @param colIndex >= 1 && <= varJson size
     */
    public void editCell(int rowIndex, int colIndex, ValueCell newValue) {
        if (hasData()) {
            if (colIndex <= valueJson.getItems().size()) {
                ValueCol col = valueJson.getItems().get(colIndex - 1);
                col.getColValues().set(rowIndex, newValue);
            }
        }
    }

    /**
     * 编辑列值
     *
     * @param colIndex
     * @param newValue
     */
    public void editColumn(int colIndex, String newValue) {
        if (hasData()) {
            if (colIndex <= valueJson.getItems().size()) {
                ValueCol col = valueJson.getItems().get(colIndex - 1);
                for (ValueCell cell : col.getColValues()) {
                    cell.setName(newValue);
                }
            }
        }
    }

    /**
     * 获取列的cells
     *
     * @param colIndex
     * @return
     */
    public List<ValueCell> getColCells(int colIndex) {
        if (hasData()) {
            if (colIndex <= valueJson.getItems().size()) {
                ValueCol col = valueJson.getItems().get(colIndex - 1);
                return col.getColValues();
            }
        }
        return null;
    }

    /**
     * 当前是否有新列
     * 如果当前有新列，则不允许添加新列
     *
     * @return 返回新列的索引，如果没有，则返回-1，新列索引
     */
    public int hasNewCol() {
        if (hasData()) {
            for (int i = 0; i < valueJson.getItems().size(); i++) {
                ValueCol col = valueJson.getItems().get(i);
                if (col.getColValues() != null && col.getColValues().size() > 0) {
                    if (default_col_name.equals(col.getColValues().get(0).getName())) {
                        return i + 1; // 因为数据比界面上的表的列索引小1，补上+1
                    }
                }
            }
        }
        return -1;
    }

    /**
     * 是否有新的一行数据
     *
     * @return 返回此行索引，如果为-1，表示没有新行
     */
    public int hasNewRow() {
        if (hasData()) {
            int rowCount = getRowCount();
            int colCount = getColCount();
            if (rowCount > 0) {
                for (int i = 0; i < rowCount; i++) {
                    boolean isHasNewRow = true;
                    for (int j = 1; j <= colCount; j++) {
                        ValueCell cell = valueJson.getItems().get(j - 1).getColValues().get(i);
                        if (!"".equals(cell.getValue())) {
                            isHasNewRow = false;
                            break;
                        }
                    }
                    if (isHasNewRow) return i;
                }
            }
        }
        return -1;
    }

    /**
     * 是否有数据
     *
     * @return
     */
    public boolean hasData() {
        if (valueJson != null) {
            return valueJson.getItems() != null && valueJson.getItems().size() > 0;
        }
        return false;
    }

    /**
     * 获取某行某列的cell值
     *
     * @param row
     * @param colIndex
     * @return
     */
    public ValueCell getCell(int row, int colIndex) {
        if (hasData()) {
            ValueCol col = valueJson.getItems().get(colIndex - 1);
            return col.getColValues().get(row);
        }
        return null;
    }

    /**
     * 粘贴到列
     *
     * @param col
     */
    public void pasteColumn(int col) {
        if (col == 0) return; // 第一列不允许复制
        if (hasData()) {
            List<CellIndex> cellIndices = ClipHelper.getData();
            if (cellIndices != null && cellIndices.size() > 0) {
                ValueCol valueCol = valueJson.getItems().get(col - 1);
                for (int i = 0; i < valueCol.getColValues().size(); i++) {
                    ValueCell valueCell = valueCol.getColValues().get(i);
                    if (i < cellIndices.size()) {
                        CellIndex index = cellIndices.get(i);
                        ValueCell sourceCell = getCell(index.getRow(), index.getCol());
                        if (sourceCell != null) {
                            valueCell.setValue(sourceCell.getValue());
                        }
                    }
                }
            }
        }
    }

    /**
     * 把所选的行复制到某行
     *
     * @param row
     */
    public void pasteRow(int row) {
        if (hasData()) {
            List<CellIndex> cellIndices = ClipHelper.getData();
            if (cellIndices != null && cellIndices.size() > 0) {
                for (int i = 0; i < cellIndices.size(); i++) {
                    CellIndex index = cellIndices.get(i);
                    ValueCell valueCell = getCell(index.getRow(), index.getCol());
                    ValueCell sourceCell = getCell(row, i + 1);
                    sourceCell.setValue(valueCell.getValue());
                }
            }
        }
    }

    /**
     * 粘贴到某个位置, 目前仅支持一个项
     *
     * @param row 该位置的行号
     * @param col 该位置的列号
     */
    public void pasteRect(int row, int col) {
        if (hasData()) {
            List<CellIndex> cellIndices = ClipHelper.getData();
            if (cellIndices != null && cellIndices.size() > 0) {
//                // 计算列数和行数
//                int selection_col_count = 0;
//                int selection_row_count = 0;
//                int current_row = cellIndices.get(0).getRow();
//                for (CellIndex index : cellIndices) {
//                    if (index.getRow() == current_row) {
//                        current_row = index.getRow();
//                        selection_col_count++;
//                    } else {
//                        break;
//                    }
//                }
//                selection_row_count = cellIndices.size() / selection_col_count;
//                System.out.println("s_c_c: " + selection_row_count + "  " + selection_col_count);
//
//                // 计算复制到的目标区域是否在表内
//                if (row + selection_row_count >=)
                if (cellIndices.size() == 1) {
                    CellIndex index = cellIndices.get(0);
                    if (col == 0) return; // ignore col 0
                    ValueCell valueCell = getCell(index.getRow(), index.getCol());
                    ValueCell soureCell = getCell(row, col);
                    soureCell.setValue(valueCell.getValue());
                }
            }
        }
    }

    /**
     * 通过cellIndex来获取一个varCell
     *
     * @param index
     * @return
     */
    public ValueCell getVarCellByCellIndex(CellIndex index) {
        if (index == null) return null;
        if (index.getCol() == 0) return null;
        return valueJson.getItems().get(index.getCol() - 1).getColValues().get(index.getRow());
    }
}
