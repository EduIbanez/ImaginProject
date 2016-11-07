package eina.imagine;

import java.util.Random;

public class Automata {

    private String id = "";
    private String data = "";

    public Automata(String id, String data) {
        this.id = id;
        this.data = data;
    }

    /**
     * Reads the state of the automaton
     */
    public State readDecision() {
        State state = new State();
        String[] parsed = data.split(";");
        int index = 0;
        while(index < parsed.length) {
            if (index == 0) {
                state.setLine(parsed[index]);
            } else if (index == 1) {
                state.setSonA(parsed[index]);
            } else if (index == 2) {
                try {
                    double prob = Double.parseDouble(parsed[index]);
                    state.setProbability(prob);
                } catch (Exception e) {
                    state.setSonB(parsed[index]);
                }
            } else if (index == 3) {
                state.setProbability(Double.parseDouble(parsed[index]));
            }
            index++;
        }
        return state;
    }

    public String nextState(State dec) {
        if (dec.getSonA() != null && dec.getSonB()!= null){
            return calculateSon(dec);
        } else if(dec.getSonB()!= null){
            return dec.getSonB();
        } else if(dec.getSonA()!= null){
            return dec.getSonA();
        }
        return null;
    }

    private String calculateSon(State dec) {
        Random ram = new Random();
        if(dec.getProbability()*100 <ram.nextInt(100)){
            return dec.getSonA();
        } else {
            return dec.getSonB();
        }
    }

    public String getPath() {
        return id.replace(".", "_");
    }

}
