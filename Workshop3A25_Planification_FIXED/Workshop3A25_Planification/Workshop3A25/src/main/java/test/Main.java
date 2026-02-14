package test;

import models.Planification;
import services.ServicePlanification;

import java.sql.Date;
import java.sql.Time;

public class Main {

    public static void main(String[] args) {
        ServicePlanification sp = new ServicePlanification();

        Planification p = new Planification(
                "meeting",
                Date.valueOf("2026-02-14"),
                Time.valueOf("10:00:00"),
                Time.valueOf("11:00:00"),
                "online",
                "planned",
                "demo insert",
                "https://meet.google.com/xxx-xxxx-xxx"
        );

        sp.add(p);
        System.out.println(sp.getAll());
    }
}
