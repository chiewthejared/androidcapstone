package com.example.test_v2.doctorInfo;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "doctors")
public class DoctorItem implements Serializable {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String userId;
    public String name;
    public String clinic;
    public String specialty;
    public String conditionsTreated;
    public String office;
    public String phone;
    public String fax;
    public String address;
    public String insurance;
    public String notes;

    public DoctorItem(String userId, String name, String clinic, String specialty,
                      String conditionsTreated, String office, String phone, String fax,
                      String address, String insurance, String notes) {
        this.userId = userId;
        this.name = name;
        this.clinic = clinic;
        this.specialty = specialty;
        this.conditionsTreated = conditionsTreated;
        this.office = office;
        this.phone = phone;
        this.fax = fax;
        this.address = address;
        this.insurance = insurance;
        this.notes = notes;
    }
}