import java.io.*;
import java.util.concurrent.RecursiveTask;

public class MatrixExtractorTask extends RecursiveTask<Matrix> implements Task{

    private File file;
    private int limit;
    private int start;
    private int end;
    private Matrix matrix;

    private boolean isPoison;

    public MatrixExtractorTask(File file, int limit, int start, int end) {
        this.file = file;
        this.limit = limit;
        this.start = start;
        this.end = end;
        this.isPoison = false;
        initMatrix();
    }

    public MatrixExtractorTask(boolean isPoison){
        this.isPoison = isPoison;
    }

    public MatrixExtractorTask(int start, int end, int limit, Matrix matrix, File file){
        this.start = start;
        this.end = end;
        this.limit = limit;
        this.matrix = matrix;
        this.file = file;
        this.isPoison = false;
    }

    private void initMatrix(){
        try(BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String firstLine = reader.readLine();

            if(firstLine == null){
                System.out.println("File is empty");
                return;
            }

            String[] parts = firstLine.split(",");
            if(parts.length != 3){
                System.out.println("Invalid line format " + firstLine);
                return;
            }

            String matrixName = parts[0].split("=")[1].trim();
            int rows = Integer.parseInt(parts[1].split("=")[1].trim());
            int cols = Integer.parseInt(parts[2].split("=")[1].trim());

            matrix = new Matrix(matrixName, rows, cols);

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public MatrixExtractorTask(int start, int end){
        this.start = start;
        this.end = end;
        this.isPoison = false;
    }

    @Override
    protected Matrix compute() {
        if(end - start <= limit){
            readFileSegment(start, end);
            return matrix;
        }

        int middle = start + (end - start) / 2;

//        System.out.println("Usao sam");

        MatrixExtractorTask left = new MatrixExtractorTask(adjustStart(start), adjustEnd(middle), limit, matrix, file);
        MatrixExtractorTask right = new MatrixExtractorTask(adjustStart(middle), adjustEnd(end), limit, matrix, file);

        left.fork();
        right.compute();
        left.join();

        return matrix;
    }


    private int adjustStart(int start){

        try(RandomAccessFile raf = new RandomAccessFile(file, "r")){
            if(start > 0){
                raf.seek(start);
                while(raf.getFilePointer() > 0 && raf.readByte() != '\n'){
                    start--;
                    raf.seek(start);
                }
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return start;
    }

    private int adjustEnd(int end){

        try(RandomAccessFile raf = new RandomAccessFile(file,"r")) {
            if(end < raf.length()){
                raf.seek(end - 1);
                while(raf.getFilePointer() < raf.length() && raf.readByte() != '\n'){
                    end++;
                }
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return end;
    }

    //ovo radi za male matrice
    private void readFileSegment(int start, int end){

        try(RandomAccessFile raf = new RandomAccessFile(file, "r")){

            raf.seek(start);
            long currentPosition = raf.getFilePointer();

            StringBuilder lineBuilder = new StringBuilder();

            // Preskačemo karaktere dok ne nađemo početak nove linije
            while (currentPosition < end && raf.readByte() != '\n') {
                currentPosition = raf.getFilePointer();
            }

            // Nastavljamo čitanje linija dok smo unutar segmenta
            while (currentPosition < end){
                char currentChar = (char) raf.readByte();
                currentPosition = raf.getFilePointer();

                lineBuilder.append(currentChar);

                if (currentChar == '\n' || currentPosition >= end) {
                    // Ako smo došli do kraja linije ili segmenta,
                    // prosleđujemo liniju na ažuriranje matrice
                    updateMatrix(lineBuilder.toString());
                    // Resetujemo StringBuilder za sledeću liniju
                    lineBuilder.setLength(0);
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void updateMatrix(String line){

        String[] parts = line.split("=");

        if(parts.length != 2){
            System.err.println("Invalid line format: " + line);
            return;
        }

        String[] coordinates = parts[0].trim().split(",");
        if(coordinates.length != 2){
            System.err.println("Invalid line format: " + line);
            return;
        }

        int row = Integer.parseInt(coordinates[0].trim());
        int col = Integer.parseInt(coordinates[1].trim());
        int value = Integer.parseInt(parts[1].trim());

        matrix.setElement(row, col, value);


    }


    @Override
    public TaskType getType() {
        return TaskType.CREATE;
    }

    public boolean isPoison() {
        return isPoison;
    }
}
