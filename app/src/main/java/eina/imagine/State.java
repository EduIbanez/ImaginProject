package eina.imagine;

public class State {
    private double probability;
    private String sonA;
    private String sonB;
    private String pathImage;
    private String line;

    public State() {}

    public State(String line, String sonA, String sonB, double probability, String pathImage) {
        this.line = line;
        this.sonA = sonA;
        this.sonB = sonB;
        this.probability = probability;
        this.pathImage = pathImage;
    }

    public String getSonA() {
        return sonA;
    }

    public void setSonA(String sonA) {
        this.sonA = sonA;
    }

    public String getSonB() {
        return sonB;
    }

    public void setSonB(String sonB) {
        this.sonB = sonB;
    }

    public String getPathImage() {
        return pathImage;
    }

    public void setPathImage(String pathImage) {
        this.pathImage = pathImage.toLowerCase();
    }

    public String getLine() {
        return line;
    }

    public void setLine(String line) {
        this.line = line;
    }

    public double getProbability() {

        return probability;
    }

    public void setProbability(double probability) {
        this.probability = probability;
    }
}
