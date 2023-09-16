package com.example.detailedBoard.Interface;

import com.example.detailedBoard.domain.Post;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

public interface PostRepositoryInterface {
    public Post createPost(Post post);
    public Post readAnyPost(Integer id);
    public List<Post> readAllPost();
    public void delete();
    public void viewCountUpdate(Integer id);
    public void likeCountUpdate(Integer id);
}
