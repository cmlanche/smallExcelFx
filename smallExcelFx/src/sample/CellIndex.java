package sample;

/**
 * Created by cmlanche on 2017/6/2.
 * cell索引
 */
public class CellIndex {

    public CellIndex() {
    }

    public CellIndex(int row, int col) {
        this.row = row;
        this.col = col;
    }

    /**
     * 行号
     */
    private int row;
    /**
     * 列号
     */
    private int col;

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getCol() {
        return col;
    }

    public void setCol(int col) {
        this.col = col;
    }

    @Override
    public String toString() {
        return "CellIndex{" +
                "row=" + row +
                ", col=" + col +
                '}';
    }
}
