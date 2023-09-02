package com.example.detailedBoard.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Post 객체 단위 테스트 입니다.
 */


class PostTest {
    @Test
    @DisplayName("Test Code 동작 확인.")
    public void test() {
        System.out.println("Test Code Test 입니다.");
    }

    // 게시글 생성 기능 테스트
    @Test
    @DisplayName("Post 객체 생성 테스트")
    public void createPost() {
        Post post = new Post();
        post.setId(1);
        post.setTitle("테스트 제목");
        post.setContent("테스트 내용");
        post.setViewCount(0);
        post.setLikeCount(0);
        post.setUsername("테스트 이름");

        // 제목이 정상적으로 입력이 되었는지
        assertEquals(post.getTitle(), "테스트 제목");

        // 내용이 정상적으로 입력이 되었는지
        assertEquals(post.getContent(), "테스트 내용");

        // 이름이 정상적으로 입력이 되었는지
        assertEquals(post.getUsername(), "테스트 이름");

        // view count 와 like count 의 default 값으로 0이 할당이 되었는지
        assertEquals(post.getViewCount(), 0);
        assertEquals(post.getLikeCount(), 0);

        // 테스트 내용과 다르게 입력이 되었다면
        assertEquals(post.getTitle(), "Exception");

    }

}