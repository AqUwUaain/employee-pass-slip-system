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
            LocalDate joinDate
    ) {

        this.id = id;

        this.firstName = firstName;

        this.lastName = lastName;

        this.department = department;

        this.position = position;

        this.contact = contact;

        this.joinDate = joinDate;

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


    @Override
    public String toString() {

        return id + " - " + firstName + " " + lastName;

    }
}