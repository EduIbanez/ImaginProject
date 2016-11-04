package eina.imagine;

public class Suceso {
    private double probability;
    private Suceso sonA;
    private Suceso sonB;
    private String pathImage;
    public Suceso(Double probability, Suceso sonA, Suceso sonB, String pathImage){
        this.sonA = sonA;
        this.sonB = sonB;
        this.probability = probability;
        this.pathImage = pathImage;
    }


}
