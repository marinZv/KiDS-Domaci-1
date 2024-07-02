import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class SaveMatrixTask implements Runnable{

    private Matrix matrix;
    private String directoryPath;
    private String fileName;

    public SaveMatrixTask(Matrix matrix, String directoryPath, String fileName){
        this.matrix = matrix;
        this.directoryPath = directoryPath;
        this.fileName = fileName;
    }


    @Override
    public void run() {

        String filePath = directoryPath + "\\" + fileName;

        try(BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {

            writer.write("matrix_name=" + matrix.getName() + ", rows=" + matrix.getNumRows() + ", cols=" + matrix.getNumCols() + "\n");

            for (int i = 0; i < matrix.getNumRows(); i++) {
                for (int j = 0; j < matrix.getNumCols(); j++) {
                    if(matrix.getElement(i, j) != 0){
                        writer.write(i + "," + j + " = " + matrix.getElement(i, j) + "\n");
                    }
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
