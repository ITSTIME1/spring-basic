package com.example.detailedBoard.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Null;
import lombok.Data;

@Data
public class LoginCustomer {
    @Id
    int id;
    String userId;
    String email;
    String password;
}
