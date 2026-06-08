package models;

import java.time.LocalDate;

public class Employee {

    private int id;

    private String firstName;

    private String lastName;

    private String department;

    private String position;

    private String contact;

    private LocalDate joinDate;

    private String manager;

    private String email;

    private String address;

    private String emergencyContact;

    // SIMPLE CONSTRUCTOR
    public Employee(
            int id,
            String firstName,
            String lastName
    ) {

        this.id = id;

        this.firstName = firstName;

        this.lastName = lastName;

    }

    // FULL CONSTRUCTOR
    public Employee(
            int id,
            String firstName,
            String lastName,
            String department,
            String position,
            String contact,
            LocalDate joinDate,
            String manager,
            String email,
            String address,
            String emergencyContact
    ) {

        this.id = id;

        this.firstName = firstName;

        this.lastName = lastName;

        this.department = department;

        this.position = position;

        this.contact = contact;

        this.joinDate = joinDate;

        this.manager = manager;

        this.email = email;

        this.address = address;

        this.emergencyContact = emergencyContact;

    }

    public int getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getDepartment() {
        return department;
    }

    public String getPosition() {
        return position;
    }

    public String getContact() {
        return contact;
    }

    public LocalDate getJoinDate() {
        return joinDate;
    }

    public String getManager() {
        return manager;
    }

    public String getEmail() {
        return email;
    }

    public String getAddress() {
        return address;
    }

    public String getEmergencyContact() {
        return emergencyContact;
    }

    @Override
    public String toString() {

        return id + " - " + firstName + " " + lastName;

    }
}
