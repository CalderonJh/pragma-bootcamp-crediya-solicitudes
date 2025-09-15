package com.co.crediya.requests.model.loanapplication;

public record Installment(
    int number,
    String dueDate,
    String principal, // abono a capital
    String interest, // pago de intereses
    String total) {}
