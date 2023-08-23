package com.example.detailedBoard.service;

import com.example.detailedBoard.domain.Post;
import com.example.detailedBoard.repository.PostRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


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
    public void createPost(Post post) {
        LocalDateTime nowDateTime = LocalDateTime.now();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formatDateTime = dateTimeFormatter.format(nowDateTime);
        post.setCurrentDatetime(formatDateTime);
        postRepository.create(post);
    }
}
