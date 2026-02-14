package model;

public class ScoreCompetence {
    private int idDetail;
    private String nomCritere;
    private float noteAttribuee;
    private String appreciationSpecifique;

    public ScoreCompetence() {}

    public ScoreCompetence(String nomCritere, float noteAttribuee, String appreciationSpecifique) {
        this.nomCritere = nomCritere;
        this.noteAttribuee = noteAttribuee;
        this.appreciationSpecifique = appreciationSpecifique;
    }

    public int getIdDetail() {
        return idDetail;
    }

    public void setIdDetail(int idDetail) {
        this.idDetail = idDetail;
    }

    public String getNomCritere() {
        return nomCritere;
    }

    public void setNomCritere(String nomCritere) {
        this.nomCritere = nomCritere;
    }

    public float getNoteAttribuee() {
        return noteAttribuee;
    }

    public void setNoteAttribuee(float noteAttribuee) {
        this.noteAttribuee = noteAttribuee;
    }

    public String getAppreciationSpecifique() {
        return appreciationSpecifique;
    }

    public void setAppreciationSpecifique(String appreciationSpecifique) {
        this.appreciationSpecifique = appreciationSpecifique;
    }

    @Override
    public String toString() {
        return "ScoreCompetence{" +
                "idDetail=" + idDetail +
                ", nomCritere='" + nomCritere + '\'' +
                ", noteAttribuee=" + noteAttribuee +
                ", appreciationSpecifique='" + appreciationSpecifique + '\'' +
                '}';
    }
}