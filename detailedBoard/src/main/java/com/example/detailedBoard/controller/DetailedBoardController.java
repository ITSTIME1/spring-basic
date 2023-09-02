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
        // view로 데이터를 넘겨주는건 index -> view 로 넘길때 조회수를 올리고 view 로 post객체들을 반환하니까
        postService.incrementViewCount(id);
        model.addAttribute("post", postService.readAnyPost(id));
        return "view";
    }


    // 좋아요를 눌렀을때 좋아요가 증가할 수 있도록 만들면 될거 같은데.
    // 그럼 좋아요를 누르게 되면 post id와 함께 오고
    @GetMapping("/like-count/{id}")
    public String likeCount(@PathVariable(value = "id") Integer id, Model model) {
        // post id 값을 받아 주고
        // post id값을 기준으로 좋아요 횟수를 증가시켜 줄건데
        // 이렇게 좋아요 누른 횟수를 반환 받고
        // view에 업데이트해준다.
        // 쿼리를 두번 실행하지 않고 redirect 함으로써 좋아요만 올려주고 페이지만 새로 고침 한다.
        postService.incrementLikeCount(id);
        // 이렇게 하면 view가 새로고침 되면서 조회수 카운트가 같이 가게 되기 때문에
        // 이를 방지하기 위해서 음 아무반응이 없게 하고 싶은데
        // 결국 hikari pool 문제는 굉장히 흥미로운데
        // 사용할 connection이 없기 때문에 발생한 문제야 이에 대해서 포스팅으로 다루면 재밌긴 하겠다.
        return "redirect:/";
    }
    // 회원 페이지
    // 로그인 페이지

}
