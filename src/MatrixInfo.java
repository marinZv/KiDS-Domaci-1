public class MatrixInfo implements Comparable<MatrixInfo>{

    private String name;
    private int rows;
    private int cols;
    private String file;

    public MatrixInfo(String name, int rows, int cols, String file) {
        this.name = name;
        this.rows = rows;
        this.cols = cols;
        this.file = file;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public int getCols() {
        return cols;
    }

    public void setCols(int cols) {
        this.cols = cols;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }


    @Override
    public int compareTo(MatrixInfo o) {
        int compareRows = Integer.compare(this.rows, o.getRows());
        if(compareRows != 0){
            return compareRows;
        }

        return Integer.compare(this.cols, o.getCols());
    }

    @Override
    public String toString() {

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(name);
        stringBuilder.append(" | rows = ");
        stringBuilder.append(rows);
        stringBuilder.append(", cols = ");
        stringBuilder.append(cols);
        stringBuilder.append(" | ");
        stringBuilder.append(file);

        return stringBuilder.toString();
    }
}
