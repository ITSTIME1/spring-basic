package com.example.detailedBoard.domain;

import jakarta.persistence.*;
import lombok.Data;

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
    private String currentDatetime;
    @Column
    private int viewCount = 0;
    @Column
    private int likeCount = 0;
}
