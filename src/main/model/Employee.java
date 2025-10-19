package main.model;

import java.util.ArrayList;

public class Employee {
    private final String name;
    private final boolean eiExempt; // owner-relative or other EI exemption

    public Employee(String name, boolean eiExempt) {
        this.name = name;
        this.eiExempt = eiExempt;
    }

    public String getName() { return name; }
    public boolean isEiExempt() { return eiExempt; }
}
