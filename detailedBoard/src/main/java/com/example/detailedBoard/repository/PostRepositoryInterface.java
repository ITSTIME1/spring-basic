package com.example.detailedBoard.repository;

import com.example.detailedBoard.domain.Post;

public interface PostRepositoryInterface {
    public void create(Post post);
    public void read();
    public void readAll();
    public void delete();
    public void update();
}
