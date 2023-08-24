package com.example.detailedBoard.controller;

import com.example.detailedBoard.domain.Post;
import com.example.detailedBoard.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
    public String indexPage(Model model) {
        // model객체에 넘겨서 index로 넘겨준다.
        model.addAttribute("list", postService.readAllPost());
        return "index";
    }

    // 글쓰기 페이지 이동
    @GetMapping("/write-post")
    public String moveWritePage() {
        return "write";
    }

    // 글 작성 하기
    @PostMapping("/post")
    public String createPost(Post post) {

        postService.createPost(post);
        return "redirect:/";
    }

    // 해당 글 보기
    @GetMapping("/view-post/{id}")
    public String viewPost(@PathVariable(value="id") Integer id, Model model) {
        // 조회수를 먼저 올리고 나서 포스트를 가지고 오자
        postService.incrementViewCount(id);
        model.addAttribute("post", postService.readAnyPost(id));
        return "view";
    }
    // 회원 페이지
    // 로그인 페이지

}
