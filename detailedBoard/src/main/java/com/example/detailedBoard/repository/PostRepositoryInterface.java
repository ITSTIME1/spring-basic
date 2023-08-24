package com.example.detailedBoard.repository;

import com.example.detailedBoard.domain.Post;

import java.util.List;

public interface PostRepositoryInterface {
    public void create(Post post);
    public Post read(Integer id);
    public List<Post> readAll();
    public void delete();
    public void viewCountUpdate(Integer id);
    public void likeCountUpdate(Integer id);
}
