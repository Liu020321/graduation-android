package com.lht.graduation;

public class Appointment {
    private String department;
    private String doctor;
    private String appointmentTime;
    private int status;

    public Appointment(String department, String doctor, String appointmentTime, int status) {
        this.department = department;
        this.doctor = doctor;
        this.appointmentTime = appointmentTime;
        this.status = status;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getDoctor() {
        return doctor;
    }

    public void setDoctor(String doctor) {
        this.doctor = doctor;
    }

    public String getAppointmentTime() {
        return appointmentTime;
    }

    public void setAppointmentTime(String appointmentTime) {
        this.appointmentTime = appointmentTime;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
