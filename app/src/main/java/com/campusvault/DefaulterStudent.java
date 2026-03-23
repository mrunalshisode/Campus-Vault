package com.campusvault;

import java.util.List;

public class DefaulterStudent {
    public String name;
    public String rollNo;
    public int overallPercent;
    public List<String> lowSubjects;

    public DefaulterStudent(String name,
                            String rollNo, int overallPercent,
                            List<String> lowSubjects) {
        this.name           = name;
        this.rollNo         = rollNo;
        this.overallPercent = overallPercent;
        this.lowSubjects    = lowSubjects;
    }
}