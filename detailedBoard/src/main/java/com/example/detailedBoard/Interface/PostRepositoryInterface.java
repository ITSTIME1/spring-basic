package com.example.detailedBoard.Interface;

import com.example.detailedBoard.domain.LoginCustomer;
import com.example.detailedBoard.domain.Post;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

public interface PostRepositoryInterface {
    Post createPost(Post post, LoginCustomer user);
    Post readAnyPost(Integer id);
    List<Post> readAllPost();
    void delete();
    void viewCountUpdate(Integer id);
    void likeCountUpdate(Integer id);
}
