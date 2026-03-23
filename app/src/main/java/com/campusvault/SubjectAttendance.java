package com.campusvault;

public class SubjectAttendance {
    public String subject;
    public int present;
    public int absent;

    public SubjectAttendance(String subject) {
        this.subject = subject;
        this.present = 0;
        this.absent  = 0;
    }

    public int getTotal() {
        return present + absent;
    }

    public int getPercentage() {
        if (getTotal() == 0) return 0;
        return (int) ((present * 100.0) / getTotal());
    }
}