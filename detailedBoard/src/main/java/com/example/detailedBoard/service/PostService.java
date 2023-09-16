package com.example.detailedBoard.service;

import com.example.detailedBoard.domain.LoginCustomer;
import com.example.detailedBoard.domain.Post;
import com.example.detailedBoard.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;


@Service
public class PostService {
    private final PostRepository postRepository;

    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    /**
     * 게시물 생성
     */
    public Post createPost(Post post, LoginCustomer user) {
        if (post.getTitle().isBlank() || post.getContent().isBlank()) {
            throw new IllegalArgumentException();
        }
        return postRepository.createPost(post, user);
    }

    /**
     * 게시물 전체 조회
     */
    public List<Post> readAllPost() {
        return postRepository.readAllPost();
    }

    /**
     * 특정 게시물 조회
     */
    public Post readAnyPost (Integer id) {
        return postRepository.readAnyPost(id);
    }

    /**
     * 게시물 조회수 증가
     */
    public void incrementViewCount(Integer id) {
        postRepository.viewCountUpdate(id);
    }

    /**
     * 게시물 좋아요 증가
     */
    public void incrementLikeCount(Integer id) {
        postRepository.likeCountUpdate(id);
    }

}
