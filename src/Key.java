public class Key {

    String matrixName;
    String multipliedBy;

    public Key(String matrixName, String multipliedBy) {
        this.matrixName = matrixName;
        this.multipliedBy = multipliedBy;
    }

    public String getMatrixName() {
        return matrixName;
    }

    public void setMatrixName(String matrixName) {
        this.matrixName = matrixName;
    }

    public String getMultipliedBy() {
        return multipliedBy;
    }

    public void setMultipliedBy(String multipliedBy) {
        this.multipliedBy = multipliedBy;
    }
}
