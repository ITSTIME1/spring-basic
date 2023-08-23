package com.example.detailedBoard.controller;

import com.example.detailedBoard.domain.Post;
import com.example.detailedBoard.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class DetailedBoardController {

    public PostService postService;

    public DetailedBoardController(PostService postService) {
        this.postService = postService;
    }

    // 초기페이지
    @GetMapping("/")
    public String indexPage() {
        return "index";
    }

    // 글쓰기 페이지
    @GetMapping("/write-post")
    public String writePage() {
        return "write";
    }

    // 글 작성 목록 받기
    @PostMapping("/post")
    public String getPost(Post post) {
        // post로 객체를 받아주고 그러면 Post에 있을테니까
        // 이제 시간을 어떻게 할지가 고민인데.
        postService.createPost(post);
        return "redirect:/";
    }
    // 회원 페이지
    // 로그인 페이지
}
