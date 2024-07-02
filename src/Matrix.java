import java.util.Arrays;

public class Matrix {

    private final int[][] data;
    private final String name;
    private final int numRows;
    private final int numCols;

    private String multipliedBy;



    public Matrix(String name, int numRows, int numCols) {
//        this.data = data;
        this.name = name;
        this.numRows = numRows;
        this.numCols = numCols;
        this.data = new int[numRows][numCols];
        multipliedBy = "None";
    }

    public int[][] getData() {
        return data;
    }

    public String getName() {
        return name;
    }

    public int getNumRows() {
        return numRows;
    }

    public int getNumCols() {
        return numCols;
    }

    public int getElement(int row, int col){
        return data[row][col];
    }

    public void setElement(int row, int col, int value){
        data[row][col] = value;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("name: ");
        stringBuilder.append(name);
        stringBuilder.append("\n");

        stringBuilder.append("numRows: ");
        stringBuilder.append(numRows);
        stringBuilder.append("\n");

        stringBuilder.append("numCols: ");
        stringBuilder.append(numCols);
        stringBuilder.append("\n");

        stringBuilder.append("multipliedBy: ");
        stringBuilder.append(getMultipliedBy());
        stringBuilder.append("\n");

        stringBuilder.append("data:");
        stringBuilder.append("\n");
        stringBuilder.append(dataString(data, numRows, numCols));

        return stringBuilder.toString();
    }

    public String dataString(int[][] data, int numRows, int numCols){
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numCols; j++) {
                stringBuilder.append(data[i][j]);
                if(j < numCols - 1){
                    stringBuilder.append(" ");
                }
            }
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }

    public void printNonZeroElementsCount(){
        int count = 0;
        for(int i = 0; i < numRows; i++){
            for(int j = 0; j < numCols; j++){
                if(data[i][j] != 0){
                    count++;
                }
            }
        }
        System.out.println("Count: " + count);
    }

    public String getMultipliedBy() {
        return multipliedBy;
    }

    public void setMultipliedBy(String multipliedBy) {
        this.multipliedBy = multipliedBy;
    }

}
