package com.example.detailedBoard.service;

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
     * 새로 생성하는 포스트는 이전 beforeDateTime이 존재하지 않고 새로운 dateTime만이 존재하기 때문에
     * 현재 currentDateTime에 시간을 설정해준 뒤 데이터베이스에 저장한다.
     */
    public Post createPost(Post post) {
        if (post.getTitle().isBlank() || post.getContent().isBlank()) {
            throw new IllegalArgumentException();
        }
        return postRepository.createPost(post);
    }

    // 모든 게시글을 가져오는 기능을 만들어야 하니까
    // 게시글을 전부 가져오는 내용은 접속 했을때 전부 가져오면 되니까
    public List<Post> readAllPost() {
        return postRepository.readAllPost();
    }

    // 해당 게시글만 하나 찾아오자.
    public Post readAnyPost (Integer id) {
        return postRepository.readAnyPost(id);
    }

    // 조회수 업데이트
    public void incrementViewCount(Integer id) {
        postRepository.viewCountUpdate(id);
    }

    public void incrementLikeCount(Integer id) {
        postRepository.likeCountUpdate(id);
    }

}
