package sample;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.StackPane;
import javafx.util.StringConverter;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    private static final int COPY_ROW = 1; // 复制行模式
    private static final int COPY_COLUMN = 2; // 复制列模式
    private static final int COPY_RECT = 3; // 复制矩形模式

    @FXML
    public TableView<ObservableList<ValueCell>> tableView;

    private MenuItem menuItemCopy;
    private MenuItem menuItemPaste;
    private MenuItem menuItemCut;

    private ContextMenu headerContextMenu;
    private ContextMenu cellContextMenu;

    /**
     * 当前所选列的索引
     */
    private int colSelectedIndex;
    /**
     * 当前行所选的索引
     */
    private int rowSelectedIndex;

    /**
     * 当前的复制类型
     */
    private int copy_type;

    @FXML
    public StackPane tabviewWrapper;

    /**
     * 当前右键弹出菜单时，鼠标指向的tablecell
     */
    private TextFieldTableCell hoverTableCell;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.initData();
        this.initContextMenu();
        this.initView();
    }

    private void initData() {
        // 添加5列数据
        List<ValueCol> cols = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            // 添加一列的数据，共8项，记为8行
            ValueCol col = new ValueCol();
            for (int j = 0; j < 5; j++) {
                ValueCell cell = new ValueCell();
                cell.setName(String.valueOf('a' + i));
                cell.setValue(cell.getName() + j);
                col.colValuesProperty().add(cell);
            }
            cols.add(col);
        }

        ValueJson valueJson = new ValueJson();
        valueJson.setItems(cols);

        ValueEditor.instance().init(valueJson);
    }

    private void initView() {
        // tabview设置
        tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        tableView.getSelectionModel().setCellSelectionEnabled(true);
        tableView.setEditable(true);
        tableView.setTableMenuButtonVisible(false);
        tableView.setMinSize(TableFxConfig.TABLE_MIN_WIDTH, TableFxConfig.TABLE_MIN_HEIGHT);
        tableView.getColumns().addListener(new ListChangeListener<TableColumn<ObservableList<ValueCell>, ?>>() {
            private boolean suspended;

            @Override
            public void onChanged(Change<? extends TableColumn<ObservableList<ValueCell>, ?>> change) {
                change.next();

                if (change.wasReplaced() && !suspended) {
                    List<TableColumn<ObservableList<ValueCell>, ?>> oldList = new ArrayList<>(change.getRemoved());
                    List<TableColumn<ObservableList<ValueCell>, ?>> newList = new ArrayList<>(tableView.getColumns());

                    // first column changed => revert to original list
                    if (oldList.get(0) != newList.get(0)) {
                        this.suspended = true;
                        tableView.getColumns().setAll(oldList);
                        this.suspended = false;
                    } else {
                        // 拖动的是其他列，此时用新列
                        List<ValueCol> cols = new ArrayList<>();
                        for (int i = 1; i < newList.size(); i++) {
                            EditColumn editColumn = (EditColumn) newList.get(i).getGraphic();
                            String title = editColumn.getTitle();

                        }
                    }
                }
            }
        });
        update();
    }

    private void initContextMenu() {
        headerContextMenu = createColumnHeaderContextMenu();
        cellContextMenu = createRowContextMenu();
    }

    /**
     * 创建一列
     *
     * @param columnIndex
     * @param col
     * @return
     */
    private TableColumn<ObservableList<ValueCell>, ValueCell> createColumn(int columnIndex, ValueCol col) {
        TableColumn<ObservableList<ValueCell>, ValueCell> column = new TableColumn<>();
        String coltext;
        if (columnIndex == 0) {
            coltext = "";
        } else {
            if (col == null || col.colValuesProperty().size() == 0) {
                coltext = "";
            } else {
                coltext = col.colValuesProperty().get(0).getName();
            }
        }
        column.setCellValueFactory(param -> {
            ObservableList<ValueCell> values = param.getValue();
            if (columnIndex >= values.size()) {
                return new SimpleObjectProperty<>(null);
            } else {
                return new SimpleObjectProperty<>(param.getValue().get(columnIndex));
            }
        });
        column.setCellFactory(param -> {
            TextFieldTableCell<ObservableList<ValueCell>, ValueCell> cell = new TextFieldTableCell<ObservableList<ValueCell>, ValueCell>() {
                @Override
                public void updateItem(ValueCell item, boolean empty) {
                    super.updateItem(item, empty);
                    if (!empty) {
                        if (getColumnIndex(getTableColumn()) == 0) {
                            Label label = new Label(item.getValue());
                            label.getStyleClass().add("first-column-text");
                            setGraphic(label);
                            setText("");
                        } else {
                            setText(item.getValue());
                        }
                    }
                }
            };
            // 转换器，把model，转换成字符串
            cell.setConverter(new StringConverter<ValueCell>() {
                @Override
                public String toString(ValueCell object) {
                    return object.getValue();
                }

                @Override
                public ValueCell fromString(String string) {
                    ValueCell valueCell = ValueEditor.instance().getCell(cell.getIndex(), getColumnIndex(cell.getTableColumn()));
                    valueCell.setValue(string);
                    return valueCell;
                }
            });
            cell.setOnMouseClicked(event -> {
                hoverTableCell = (TextFieldTableCell) event.getSource();
                if (getColumnIndex(hoverTableCell.getTableColumn()) == 0) {
                    if (event.isShiftDown()) { // 如果发现shift已经按下，则不执行行选择
                        return;
                    }
                    selectRow(cell.getIndex());

                    List<CellIndex> choosenRectCells = ClipHelper.getChoosenRect(tableView, hoverTableCell);
                    tableView.getSelectionModel().clearSelection();
                    if (choosenRectCells != null) {
                        for (CellIndex index : choosenRectCells) {
                            tableView.getSelectionModel().select(index.getRow(),
                                    tableView.getColumns().get(index.getCol()));
                        }
                    }

                } else
                    // 找到当前右键弹出的cell
                    if (event.getButton() == MouseButton.SECONDARY) {
                        if (event.getSource() instanceof TextFieldTableCell) {
                            hoverTableCell = (TextFieldTableCell) event.getSource();
                            // 所选择的矩形区域
                            List<CellIndex> choosenRectCells = ClipHelper.getChoosenRect(tableView, hoverTableCell);
                            tableView.getSelectionModel().clearSelection();
                            if (choosenRectCells != null) {
                                for (CellIndex index : choosenRectCells) {
                                    tableView.getSelectionModel().select(index.getRow(),
                                            tableView.getColumns().get(index.getCol()));
                                }
                            }
                        } else {
                            hoverTableCell = null;
                        }
                    } else {
                        hoverTableCell = null;
                    }
            });
            if (column.isEditable()) {
                Tooltip tooltip = new Tooltip();
                tooltip.textProperty().bindBidirectional(cell.textProperty());
                tooltip.setWrapText(true);
                tooltip.setMaxWidth(300);
                cell.setTooltip(tooltip);
            }

            cell.setContextMenu(cellContextMenu);
            cell.setWrapText(true);
            return cell;
        });
        if (columnIndex == 0) {
            column.setGraphic(new Label("gra"));
        }
        column.setEditable(columnIndex != 0); // 首列不允许编辑
        column.setPrefWidth(column.isEditable() ? 100 : 48);
        column.setSortable(false);

        // 设置可编辑的列
        EditColumn editColumn = new EditColumn(column);
        editColumn.setEditable(columnIndex != 0);
        editColumn.setTitle(coltext);
        editColumn.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                colSelectedIndex = getColumnIndex(column);
                headerContextMenu.show(editColumn, event.getScreenX(), event.getScreenY());
                selectCol(column);
            } else if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() >= 2) {
                // 双击编辑
                editColumn.setEditing(true);
            } else if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 1) {
                // 单击选择列
                selectCol(column);
            }
        });
        editColumn.setEditCallback(new EditColumnCallback() {
            @Override
            public void startEdit() {

            }

            @Override
            public void editCommit(TableColumn tableColumn, String newValue) {
                int colIndex = getColumnIndex(tableColumn);
                ValueEditor.instance().editColumn(colIndex, newValue);
            }

            @Override
            public void cancelEdit() {

            }
        });
        editColumn.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                // editCell commit
                System.out.println(editColumn.getTitle());
            }
        });

        column.setGraphic(editColumn);
        return column;
    }

    private int getColumnIndex(TableColumn column) {
        for (int i = 0; i < tableView.getColumns().size(); i++) {
            if (column == tableView.getColumns().get(i)) {
                return i;
            }
        }
        return 0;
    }

    /**
     * 选择一行
     * 除第一列不让选择
     *
     * @param row
     */
    private void selectRow(int row) {
        tableView.getSelectionModel().select(row);
    }

    /**
     * 选择一列
     *
     * @param column
     */
    private void selectCol(TableColumn column) {
        tableView.getSelectionModel().clearSelection();
        for (int i = 0; i < ValueEditor.instance().getRowCount(); i++)
            tableView.getSelectionModel().select(i, column);
    }

    /**
     * 创建列菜单
     *
     * @return
     */
    private ContextMenu createColumnHeaderContextMenu() {
        ContextMenu contextMenu = new ContextMenu();
        menuItemCopy = new MenuItem("复制");
        menuItemCopy.setOnAction(event -> {
            copy_type = COPY_COLUMN;
            ClipHelper.copy(tableView);
        });
        menuItemPaste = new MenuItem("粘贴");
        menuItemPaste.setOnAction(event -> {
            if (copy_type == COPY_COLUMN) {
                // paste column
                ValueEditor.instance().pasteColumn(colSelectedIndex);
                tableView.refresh();
            } else {
                System.out.println("此前的复制不是列，不能粘贴到列");
            }
        });
        menuItemCut = new MenuItem("剪贴");
        MenuItem colInsertLeft = new MenuItem("往左侧插入一列");
        colInsertLeft.setOnAction(event -> {
            addCol(true);
        });
        MenuItem colInsertRight = new MenuItem("往右侧插入一列");
        colInsertRight.setOnAction(event -> {
            addCol(false);
        });
        MenuItem colDelete = new MenuItem("删除列");
        colDelete.setOnAction(event -> {
            ValueEditor.instance().deleteCol(colSelectedIndex);
            update();
        });
        contextMenu.getItems().addAll(menuItemCopy, menuItemPaste, menuItemCut, new SeparatorMenuItem(),
                colInsertLeft, colInsertRight, colDelete);
        return contextMenu;
    }

    /**
     * 创建行菜单
     *
     * @return
     */
    private ContextMenu createRowContextMenu() {
        ContextMenu contextMenu = new ContextMenu();
        menuItemCopy = new MenuItem("复制");
        menuItemCopy.setOnAction(event -> {
            // 复制所选的区域cell
            ClipHelper.copy(tableView);
            if (hoverTableCell != null) {
                if (getColumnIndex(hoverTableCell.getTableColumn()) == 0) {
                    // 如果是鼠标是第一列的话，则视作复制行
                    copy_type = COPY_ROW;
                } else {
                    // 视作复制某个矩形区域
                    copy_type = COPY_RECT;
                }
            }
        });
        menuItemPaste = new MenuItem("粘贴");
        menuItemPaste.setOnAction(event -> {
            switch (copy_type) {
                case COPY_ROW:
                    // 采用粘贴行
                    ValueEditor.instance().pasteRow(hoverTableCell.getIndex());
                    tableView.refresh();
                    break;
                case COPY_RECT:
                    // 采用粘贴矩形
                    System.out.println("paste rect cells");
                    ValueEditor.instance().pasteRect(hoverTableCell.getIndex(),
                            getColumnIndex(hoverTableCell.getTableColumn()));
                    tableView.refresh();
                    break;
            }
        });
        menuItemCut = new MenuItem("剪贴");
        MenuItem rowInsertUp = new MenuItem("往上插入一行");
        rowInsertUp.setOnAction(event -> {
            addRow(true);
        });
        MenuItem rowInsertBottom = new MenuItem("往下插入一行");
        rowInsertBottom.setOnAction(event -> {
            addRow(false);
        });
        MenuItem rowDelete = new MenuItem("删除行");
        rowDelete.setOnAction(event -> {
            int sel = tableView.getSelectionModel().getFocusedIndex();
            ValueEditor.instance().deleteRow(sel);
            update();
        });
        contextMenu.getItems().addAll(menuItemCopy, menuItemPaste, menuItemCut, new SeparatorMenuItem(),
                rowInsertUp, rowInsertBottom, rowDelete);
        return contextMenu;
    }

    private void update() {
        tableView.getColumns().clear();
        tableView.getItems().clear();

        List<ValueCol> colNames = ValueEditor.instance().getCols();
        // 第一列
        TableColumn<ObservableList<ValueCell>, ValueCell> col0 =
                createColumn(0, null);
        tableView.getColumns().add(col0);
        // 其他列
        for (int i = 0; i < colNames.size(); i++) {
            TableColumn<ObservableList<ValueCell>, ValueCell> column =
                    createColumn(i + 1, colNames.get(i));
            tableView.getColumns().add(column);
        }
        List<ObservableList<ValueCell>> dataSource = ValueEditor.instance().getDataSource();
        if (dataSource != null) {
            tableView.getItems().addAll(dataSource);
        }
    }

    @FXML
    public void saveData(ActionEvent event) {
        ValueEditor.instance().saveData();
    }

    /**
     * 获取指定列的可编辑表头
     *
     * @param colIndex
     * @return
     */
    public EditColumn getEditColumn(int colIndex) {
        TableColumn column = tableView.getColumns().get(colIndex);
        return (EditColumn) column.getGraphic();
    }

    /**
     * 添加一列
     *
     * @param isLeft
     */
    public void addCol(boolean isLeft) {
        int nColIndex = ValueEditor.instance().hasNewCol();
        if (nColIndex == -1) {
            nColIndex = ValueEditor.instance().addNewCol(colSelectedIndex, isLeft);
            update();
        }
        // todo 聚焦此列，列头可编辑
        EditColumn editColumn = getEditColumn(nColIndex);
        editColumn.setEditing(true);
        editColumn.setTextFieldFocus(true);
    }

    /**
     * 添加一行
     *
     * @param isUp
     */
    public void addRow(boolean isUp) {
        int row = ValueEditor.instance().hasNewRow();
        if (row == -1) {
            int sel = tableView.getSelectionModel().getSelectedIndex();
            System.out.println(sel);
            row = ValueEditor.instance().addRow(sel, isUp);
            update();
        }
        // todo 让行第一个cell的编辑状态聚焦
    }
}
