package models;

import java.sql.Date;
import java.sql.Time;

public class Planification {
    private int idEvent;
    private String typeEvent;
    private Date date;
    private Time heureDebut;
    private Time heureFin;
    private String mode;
    private String statut;
    private String description;
    private String lienMeeting;

    public Planification() {}

    // INSERT constructor (no id)
    public Planification(String typeEvent, Date date, Time heureDebut, Time heureFin,
                         String mode, String statut, String description, String lienMeeting) {
        this.typeEvent = typeEvent;
        this.date = date;
        this.heureDebut = heureDebut;
        this.heureFin = heureFin;
        this.mode = mode;
        this.statut = statut;
        this.description = description;
        this.lienMeeting = lienMeeting;
    }

    // FULL constructor (with id)
    public Planification(int idEvent, String typeEvent, Date date, Time heureDebut, Time heureFin,
                         String mode, String statut, String description, String lienMeeting) {
        this.idEvent = idEvent;
        this.typeEvent = typeEvent;
        this.date = date;
        this.heureDebut = heureDebut;
        this.heureFin = heureFin;
        this.mode = mode;
        this.statut = statut;
        this.description = description;
        this.lienMeeting = lienMeeting;
    }

    public int getIdEvent() { return idEvent; }
    public void setIdEvent(int idEvent) { this.idEvent = idEvent; }

    public String getTypeEvent() { return typeEvent; }
    public void setTypeEvent(String typeEvent) { this.typeEvent = typeEvent; }

    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }

    public Time getHeureDebut() { return heureDebut; }
    public void setHeureDebut(Time heureDebut) { this.heureDebut = heureDebut; }

    public Time getHeureFin() { return heureFin; }
    public void setHeureFin(Time heureFin) { this.heureFin = heureFin; }

    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLienMeeting() { return lienMeeting; }
    public void setLienMeeting(String lienMeeting) { this.lienMeeting = lienMeeting; }
}
