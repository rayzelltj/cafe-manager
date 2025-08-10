package main.model;

import java.util.ArrayList;

public class Employee {
    private final String name;
    private ArrayList<PayStub> payStubHistory;

    public Employee(String name) {
        this.name = name;
        this.payStubHistory = new ArrayList<>();
    }

    public PayStub getLatestPayStub() {
        if (payStubHistory.isEmpty()) return null;
        return payStubHistory.get(payStubHistory.size() - 1);
    }

    public void addPayStub(PayStub stub) {
        this.payStubHistory.add(stub);
    }
}
