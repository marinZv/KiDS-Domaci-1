//import java.util.concurrent.RecursiveTask;
//
//public class MatrixMultiplierTask extends RecursiveTask<Matrix> {
//
//    private final Matrix matrixA;
//    private final Matrix matrixB;
//    private final Matrix resultMatrix;
//    private final int startRowA;
//    private final int startColA;
//    private final int startColB;
//
//    private final int startRowResultMatrix;
//    private final int numRows;
//    private final int numColsA;
//    private final int numColsB;
//
//
//    public MatrixMultiplierTask(Matrix matrixA, Matrix matrixB, Matrix resultMatrix, int startRowA, int startColA,
//                            int startColB, int startRowResultMatrix, int numRows, int numColsA, int numColsB) {
//        this.matrixA = matrixA;
//        this.matrixB = matrixB;
//        this.resultMatrix = resultMatrix;
//        this.startRowA = startRowA;
//        this.startColA = startColA;
//        this.startColB = startColB;
//        this.startRowResultMatrix = startRowResultMatrix;
//        this.numRows = numRows;
//        this.numColsA = numColsA;
//        this.numColsB = numColsB;
//    }
//
//    @Override
//    protected Matrix compute() {
//        if(numRows == 1 && numColsB == 1){
//
//            //resultMatrix.setElement(startRowResultMatrix, startColB, matrixA.getElement(startRowA, startColA) * matrixB.getElement(startColA, startColB));
//
//            if (startRowResultMatrix < resultMatrix.getNumRows() && startColB < resultMatrix.getNumCols()) {
//                resultMatrix.setElement(startRowResultMatrix, startColB, matrixA.getElement(startRowA, startColA) * matrixB.getElement(startColA, startColB));
//            }
//
//            return resultMatrix;
//        }else{
//            if(numRows >= numColsB){
//                //segmentacija matrice A po redovima
//                int midRowA = startRowA + numRows / 2;
//                MatrixMultiplierTask top = new MatrixMultiplierTask(matrixA, matrixB, resultMatrix,
//                        startRowA, startColA, startColB, startRowResultMatrix, midRowA - startRowA, numColsA, numColsB);
//
//                MatrixMultiplierTask bottom = new MatrixMultiplierTask(matrixA, matrixB, resultMatrix,
//                        midRowA, startColA, startColB, startRowResultMatrix + midRowA - startRowA, numRows - (midRowA - startRowA), numColsA, numColsB);
//
//                top.fork();
//                Matrix bottomResult = bottom.compute();
//                Matrix topResult = top.join();
//                combineResults(topResult, bottomResult, midRowA - startRowA, numColsB);
//            }else{
//                //Segmentacija matrice B po kolonama
//                int midColB = startColB + numColsB / 2;
//                MatrixMultiplierTask left = new MatrixMultiplierTask(matrixA, matrixB, resultMatrix,
//                        startRowA, startColA, startColB, startRowResultMatrix, numRows, numColsA, midColB - startColB);
//                MatrixMultiplierTask right = new MatrixMultiplierTask(matrixA, matrixB, resultMatrix,
//                        startRowA, startColA, midColB, startRowResultMatrix, numRows, numColsA, numColsB - (midColB - startColB));
//
//                left.fork();
//                Matrix rightResult = right.compute();
//                Matrix leftResult = left.join();
//                combineResults(leftResult, rightResult, numRows, midColB - startColB);
//            }
//
//        }
//        return resultMatrix;
//    }
//
//    private void combineResults(Matrix matrix1, Matrix matrix2, int numRows, int numCols){
//        // Kombinovanje rezultata iz segmentiranih matrica
//
//        for(int i = 0; i < numRows; i++){
//            for(int j = 0; j < numCols; j++){
//                if (startRowResultMatrix + i < resultMatrix.getNumRows() && startColB + j < resultMatrix.getNumCols()) {
//                    resultMatrix.setElement(startRowResultMatrix + i, startColB + j, matrix1.getElement(i, j) * matrix2.getElement(j, 0));
//                }
//                //resultMatrix.setElement(startRowResultMatrix + i, startColB + j, matrix1.getElement(i, j) * matrix2.getElement(j, 0));
//            }
//        }
//    }
//}
//
//

import java.util.concurrent.RecursiveTask;

public class MatrixMultiplierTask extends RecursiveTask<Matrix> implements Task{

    private Matrix matrixA;
    private Matrix matrixB;
    private Matrix resultMatrix;
    private int startRowA;
    private int startColA;
    private int startColB;
    private int startRowResultMatrix;
    private int numRows;
    private int numColsA;
    private int numColsB;
    private boolean isPoison;

    public MatrixMultiplierTask(Matrix matrixA, Matrix matrixB, Matrix resultMatrix, int startRowA, int startColA,
                                int startColB, int startRowResultMatrix, int numRows, int numColsA, int numColsB) {
        this.matrixA = matrixA;
        this.matrixB = matrixB;
        this.resultMatrix = resultMatrix;
        this.startRowA = startRowA;
        this.startColA = startColA;
        this.startColB = startColB;
        this.startRowResultMatrix = startRowResultMatrix;
        this.numRows = numRows;
        this.numColsA = numColsA;
        this.numColsB = numColsB;
        this.isPoison = false;
    }

    public MatrixMultiplierTask(boolean isPoison){
        this.isPoison = isPoison;
    }

    @Override
    protected Matrix compute() {
        if (numRows == 1 && numColsB == 1) {
            // Množenje matrica kada su dimenzije dovedene do 1x1
            int value = 0;
            for (int i = 0; i < numColsA; i++) {
                value += matrixA.getElement(startRowA, startColA + i) * matrixB.getElement(i, startColB);
            }
            resultMatrix.setElement(startRowResultMatrix, startColB, value);
            return resultMatrix;
        } else {
            if (numRows >= numColsB) {
                // Deljenje prve matrice po redovima
                int midRowA = startRowA + numRows / 2;
                MatrixMultiplierTask top = new MatrixMultiplierTask(matrixA, matrixB, resultMatrix,
                        startRowA, startColA, startColB, startRowResultMatrix, midRowA - startRowA, numColsA, numColsB);
                MatrixMultiplierTask bottom = new MatrixMultiplierTask(matrixA, matrixB, resultMatrix,
                        midRowA, startColA, startColB, startRowResultMatrix + midRowA - startRowA, numRows - (midRowA - startRowA), numColsA, numColsB);
                top.fork();
                Matrix bottomResult = bottom.compute();
                Matrix topResult = top.join();
            } else {
                // Deljenje druge matrice po kolonama
                int midColB = startColB + numColsB / 2;
                MatrixMultiplierTask left = new MatrixMultiplierTask(matrixA, matrixB, resultMatrix,
                        startRowA, startColA, startColB, startRowResultMatrix, numRows, numColsA, midColB - startColB);
                MatrixMultiplierTask right = new MatrixMultiplierTask(matrixA, matrixB, resultMatrix,
                        startRowA, startColA, midColB, startRowResultMatrix, numRows, numColsA, numColsB - (midColB - startColB));
                left.fork();
                Matrix rightResult = right.compute();
                Matrix leftResult = left.join();
            }
        }

        resultMatrix.setMultipliedBy(matrixA.getName() + "," + matrixB.getName());
        return resultMatrix;
    }


    @Override
    public TaskType getType() {
        return TaskType.MULTIPLY;
    }

    public boolean isPoison() {
        return isPoison;
    }
}

