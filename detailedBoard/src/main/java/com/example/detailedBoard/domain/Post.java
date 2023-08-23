package com.example.detailedBoard.domain;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column
    private String username;
    @Column
    private String title;
    @Column
    private String content;
    @Column
    private String beforeDatetime;
    @Column
    private String currentDatetime;
}