package sample;

import com.alibaba.fastjson.JSONObject;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cmlanche on 2017/6/1.
 * 复制剪贴帮助类
 */
public class ClipHelper {

    /**
     * 获取所选的Cell
     * 规则：
     * 只复制当前鼠标所在矩形区域的cells，其他的cell去掉
     *
     * @param tableView
     * @param hoverTableCell 指定右键所选择的矩形区域的cell，塞选不在此区域的cell
     */
    public static List<CellIndex> getChoosenRect(TableView tableView, TextFieldTableCell hoverTableCell) {
        if (hoverTableCell != null) {
            int hover_row_index = hoverTableCell.getIndex();
            int hover_column_index = getColumnIndex(tableView, hoverTableCell.getTableColumn());
            System.out.println("hovered--->" + new CellIndex(hover_row_index, hover_column_index));

            List<TablePosition> cells = tableView.getSelectionModel().getSelectedCells();
            List<TablePosition> choosenCells = new ArrayList<>();
            if (cells != null && cells.size() > 0) {
                TablePosition prePosition = null; // 前一个位置
                boolean isRectEnd = false; // 是否到了矩形结束的位置
                int rectColCount = 0; // 矩形的列数
                boolean isRowEnd = false; // 检测列数的时候，是否到了最后一个
                boolean isFindHoverPos = false; // 在寻找矩形框的时候，是否找到了对应的鼠标所在位置的cell，如果没找到，丢弃这个矩形选择区域
                for (TablePosition position : cells) {
                    if (prePosition == null) {
                        prePosition = position;
                        choosenCells.add(position);
                        isRectEnd = false;
                        isRowEnd = false;
                        rectColCount = 1;
                    } else {
                        if (prePosition.getRow() == hover_row_index && prePosition.getColumn() == hover_column_index) {
                            isFindHoverPos = true;
                        }
                        if (position.getRow() == prePosition.getRow() &&
                                position.getColumn() - prePosition.getColumn() == 1) {
                            // 前一个位置与这个位置是同一行
                            // 并且，这个位置与此前一个位置是相邻的，即列索引 > 1
                            prePosition = position;
                            choosenCells.add(position);
                            isRectEnd = false;
                            if (!isRowEnd) rectColCount++; // 如果没有到列尾，则++
                        } else if (position.getRow() - prePosition.getRow() == 1 &&
                                prePosition.getColumn() - position.getColumn() == (rectColCount - 1)) {
                            // 这个判断条件是说，当前这个位置在下一行的矩形第一个
                            prePosition = position;
                            choosenCells.add(position);
                            isRectEnd = false;
                            isRowEnd = true;
                        } else {
                            // 矩形计算终点
                            isRectEnd = true;
                        }
                    }

                    if (isRectEnd) {
                        if (isFindHoverPos) {
                            // 寻找矩形结束，并且找到了鼠标所选择的位置
                            System.out.println("寻找矩形结束，并且找到了鼠标所选择的位置");
                            break;
                        } else {
                            // 寻找矩形结束，但是没有找到鼠标hover的位置
                            System.out.println("寻找矩形结束，但是没有找到鼠标hover的位置");
//                            for (TablePosition unselectedPos : choosenCells) {
//                                tableView.getSelectionModel().clearAndSelect(
//                                        unselectedPos.getRow(), unselectedPos.getTableColumn());
//                            }
                            choosenCells.clear();
                            rectColCount = 1;
                            isRowEnd = false;
                            prePosition = position;
                            choosenCells.add(position);
                            isRectEnd = false;
                        }
                    }
                }

                // 把所选择的cells，转换成位置
                List<CellIndex> cellIndices = new ArrayList<>();
                for (TablePosition pos : choosenCells) {
                    if (pos.getColumn() == 0) continue; // 过滤掉第一列的数据（第一列不允许复制）
                    cellIndices.add(new CellIndex(pos.getRow(), pos.getColumn()));
                }
                System.out.println("所选择矩形的列数：" + rectColCount);
                System.out.println("经过塞选后选择的矩形区域的cells个数：" + cellIndices.size()
                        + "  数据为:\r\n" + cellIndices);
                return cellIndices;
            }
        }

        return null;
    }

    /**
     * 复制所选的数据
     *
     * @param tableView
     */
    public static void copy(TableView tableView) {
        ObservableList<TablePosition> positions = tableView.getSelectionModel().getSelectedCells();
        List<CellIndex> indices = new ArrayList<>();
        if (positions != null && positions.size() > 0) {
            for (TablePosition pos : positions) {
                if (pos.getColumn() == 0) continue; // 忽略掉第一列
                indices.add(new CellIndex(pos.getRow(), pos.getColumn()));
            }
            copy(JSONObject.toJSONString(indices));
        }
    }

    /**
     * 获取某列的索引值
     *
     * @param tableView
     * @param column
     * @return
     */
    private static int getColumnIndex(TableView tableView, TableColumn column) {
        for (int i = 0; i < tableView.getColumns().size(); i++) {
            if (column == tableView.getColumns().get(i)) {
                return i;
            }
        }
        return 0;
    }

//    public static void paste()

    /**
     * 剪贴所选的cell
     *
     * @param tableView
     */
    public static void cut(TableView tableView) {

    }

    /**
     * 复制某行某列
     *
     * @param row
     * @param col
     */
    private static void copyCell(int row, int col) {
        List<CellIndex> cells = new ArrayList<>();
        CellIndex cellIndex = new CellIndex(row, col);
        cells.add(cellIndex);
        copy(JSONObject.toJSONString(cells));
    }

    /**
     * 复制行
     *
     * @param row
     */
    public static void copyRow(int row) {
        List<CellIndex> cells = new ArrayList<>();
        int colCount = ValueEditor.instance().getColCount();
        for (int i = 1; i < colCount; i++) {
            CellIndex cellIndex = new CellIndex(row, i);
            cells.add(cellIndex);
        }
        copy(JSONObject.toJSONString(cells));
    }


    /**
     * 复制数据
     *
     * @param data
     */
    private static void copy(String data) {
        Clipboard clipboard = Clipboard.getSystemClipboard();
        clipboard.clear();
        ClipboardContent clipboardContent = new ClipboardContent();
        clipboardContent.putString(data);
        clipboard.setContent(clipboardContent);
        System.out.println("复制到的数据：" + data);
    }

    /**
     * 获取剪贴板的数据
     *
     * @return
     */
    public static List<CellIndex> getData() {
        Clipboard clipboard = Clipboard.getSystemClipboard();
        String data = clipboard.getString();
        if (null != data && !"".equals(data)) {
            try {
                return JSONObject.parseArray(data, CellIndex.class);
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }
        return null;
    }
}
