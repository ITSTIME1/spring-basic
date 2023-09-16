package com.example.detailedBoard.controller;

import com.example.detailedBoard.domain.ErrorResponse;
import com.example.detailedBoard.domain.LoginCustomer;
import com.example.detailedBoard.domain.Post;
import com.example.detailedBoard.service.CustomerService;
import com.example.detailedBoard.service.PostService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.apache.coyote.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class DetailedBoardController {

    private PostService postService;
    private CustomerService customerService;
    private final Logger log = LoggerFactory.getLogger(getClass());

    public DetailedBoardController(PostService postService, CustomerService customerService) {
        this.postService = postService;
        this.customerService = customerService;
    }

    /**
     * indexPage = 초기페이지 (모든 게시물을 조회함)
     */
    @GetMapping("/")
    public String mainPage(Model model) {
        // model객체에 넘겨서 index로 넘겨준다.
        model.addAttribute("list", postService.readAllPost());
        return "index";
    }

    /**
     * moveWritePage = 게시물 작성 페이지로 이동하기
     */
    @GetMapping("/write-post")
    public String writePage(HttpServletRequest request) {
        HttpSession session = request.getSession();
        LoginCustomer user = (LoginCustomer) session.getAttribute("loginUser");
        if (user == null) {
            throw new NullPointerException();
        }
        return "write";
    }


    /**
     * createPost = 게시물 생성
     */
    @PostMapping("/post")
    public String createPost(HttpServletRequest request, Post post, Model model) {

        HttpSession session = request.getSession();
        LoginCustomer user = (LoginCustomer) session.getAttribute("loginUser");
        if (user == null) {
            throw new NullPointerException();
        }
        // post 객체와 세션ID를 통해 userId를 얻어옴
        // 그리고 post 갯수도 올려야 하니까 Id값을 기준으로 올려도 될거 같은데
        // 그럼 LoginCustomer 값을 보내자.
        Post result = postService.createPost(post, user);
        // 포스트가 정상적으로 생성이 되었다면 유저의 post값도 올린다.
        customerService.incrementUserPostCount(user.getUserId());
        model.addAttribute("post", result);
        return "view";
    }

    /**
     * viewPost = 특정 게시물 가져오기, 게시물을 가져올때 해당 게시물의 조회수를 1 증가
     */
    @GetMapping("/view-post/{id}")
    public String viewPost(@PathVariable(value = "id") Integer id, Model model, HttpServletRequest request) {
        HttpSession session = request.getSession();
        LoginCustomer user = (LoginCustomer) session.getAttribute("loginUser");
        // user 객체가 null이라면 로그인을 하지 않은 상태를 의미.
        // 따라서 profile에 접근할때 로그아웃 상태라면 로그인된 상태가 아니기 때문에 보여주지 못하는 페이지를 만들어서
        // 보여줄 수도 있음.
        if (user == null) {
            throw new NullPointerException();
        }
        // 조회수를 먼저 올리고 나서 포스트를 가지고 오자
        // view로 데이터를 넘겨주는건 index -> view 로 넘길때 조회수를 올리고 view 로 post객체들을 반환하니까
        postService.incrementViewCount(id);
        model.addAttribute("post", postService.readAnyPost(id));
        return "view";
    }


    /**
     * likeCount = 좋아요를 클릭했을 경우, 좋아요 횟수를 올림
     */
    @GetMapping("/like-count/{id}")
    public String likeCount(@PathVariable(value = "id") Integer id, Model model) {
        postService.incrementLikeCount(id);
        return "redirect:/view-post/" + id;
    }


    /**
     * RuntimeException Handler = IllegalException, RuntimeException을 처리합니다.
     * ErrorResponseDTO를 공통 에러 객체로 활용하여 리턴하게 됩니다.
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRunTimeException(RuntimeException e) {

        if (e instanceof IllegalArgumentException) {
            return ResponseEntity.ok(
                    ErrorResponse.of("Invaild Input",
                            HttpStatus.BAD_REQUEST.value(),
                            "값이 둘 중 하나가 비어 있거나 둘다 비어 있습니다.",
                            "값을 반드시 모두 입력 해야 합니다."));
        } else {
            return ResponseEntity.ok(
                    ErrorResponse.of("Failed Access",
                            HttpStatus.BAD_REQUEST.value(),
                            "조회가 실패 하였습니다.",
                            "다시 시도해 주십시오."));
        }
    }
}
