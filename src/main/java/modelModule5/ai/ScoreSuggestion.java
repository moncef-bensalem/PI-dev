package modelModule5.ai;

public class ScoreSuggestion {
    private String nomCritere;
    private double noteAttribuee;
    private String appreciationSpecifique;

    public ScoreSuggestion() {}

    public ScoreSuggestion(String nomCritere, double noteAttribuee, String appreciationSpecifique) {
        this.nomCritere = nomCritere;
        this.noteAttribuee = noteAttribuee;
        this.appreciationSpecifique = appreciationSpecifique;
    }

    public String getNomCritere() {
        return nomCritere;
    }

    public void setNomCritere(String nomCritere) {
        this.nomCritere = nomCritere;
    }

    public double getNoteAttribuee() {
        return noteAttribuee;
    }

    public void setNoteAttribuee(double noteAttribuee) {
        this.noteAttribuee = noteAttribuee;
    }

    public String getAppreciationSpecifique() {
        return appreciationSpecifique;
    }

    public void setAppreciationSpecifique(String appreciationSpecifique) {
        this.appreciationSpecifique = appreciationSpecifique;
    }
}

