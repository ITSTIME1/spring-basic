package com.example.detailedBoard.controller;

import com.example.detailedBoard.domain.LoginCustomer;
import com.example.detailedBoard.domain.RegisterCustomer;
import com.example.detailedBoard.domain.ErrorResponse;
import com.example.detailedBoard.service.CustomerService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@Controller
public class CustomerController {

    private final CustomerService customerService;
    private final Logger log = LoggerFactory.getLogger(getClass());

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }


    /**
     * 회원가입 페이지
     */
    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    /**
     * 로그인 페이지
     */
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }
    /**
     * 프로필 페이지
     */
//    @GetMapping("/profile")
//    public String profilePage(@CookieValue(name = "id", required = false) String id) {
//        // 만약 로그인을 하지 않았다면 id값이 비어 있을테니까
//        // 처음 페이지로 보내준다.
//        log.info("쿠키 값 : " + id);
//        if (id == null) {
//            log.info("null");
//            return "redirect:/";
//        } else {
//            // 만약 로그인을 진행한 상태라면
//            return "profile";
//        }
//    }
    @GetMapping("/profile")
    public String profilePage(HttpServletRequest request) {
        HttpSession session = request.getSession();
        LoginCustomer user = (LoginCustomer) session.getAttribute("loginUser");
        // user 객체가 null이라면 로그인을 하지 않은 상태를 의미.
        // 따라서 profile에 접근할때 로그아웃 상태라면 로그인된 상태가 아니기 때문에 보여주지 못하는 페이지를 만들어서
        // 보여줄 수도 있음.
        if (user == null) {
            throw new NullPointerException();
        } else {
            return "profile";
        }
    }

    /**
     * 로그인 요청
     * 쿠키 로그인 요청 o
     * 세션 로그인 요청 o
     * spring security 다해보자
     */
//    @PostMapping("/login/user")
//    public String userLogin(@CookieValue(name = "id", required = false) String id, LoginCustomer loginCustomer, HttpServletResponse response) {
//        // 처음에는 쿠키 정보가 없기 때문에 required false 설정을 해준다.
//        LoginCustomer userInfo = customerService.loginUser(loginCustomer.getEmail(), loginCustomer.getPassword());
//        if (id == null) {
//            Cookie userCookie = new Cookie("id", String.valueOf(userInfo.getId()));
//            userCookie.setPath("/");
//            response.addCookie(userCookie);
//            log.info("쿠키 생성 완료 : " + userCookie.getValue());
//        } else{
//            log.info("쿠키가 이미 존재 합니다");
//        }
//
//        return "redirect:/";
//    }


    /**
     * 세션 로그인
     */
    // 세션 로그인 한번 해보자.
    @PostMapping("/login/user")
    public String userLogin(HttpServletRequest request, LoginCustomer loginCustomer) {
        // HttpServletRequest에서 session을 얻는다.

        LoginCustomer userInfo = customerService.loginUser(loginCustomer.getEmail(), loginCustomer.getPassword());

        HttpSession session = request.getSession(true);
        // 이제 세션에 유저에 대한 정보를 저장함.
        // 만약에 세션정보를 가지고 있지 않다면 생성하고, 만약 세션 정보를 가지고 있다면 세션 정보를 반환한다.
        // request.getSession()
        session.setAttribute("loginUser", userInfo);
        // 1분 = 60초
        // 30분 = 1800초
        session.setMaxInactiveInterval(60);
        log.info(String.valueOf(session.getAttribute("loginUser")));
        return "redirect:/";
    }

    /**
     * 로그아웃
     */
    @RequestMapping("/logout")
    public String userLogout(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        // 세션이 이미 삭제되어 있다면
        if (session == null) {
            throw new NullPointerException();
        } else {
            session.invalidate();
            log.info("세션 성공적으로 삭제");
        }
//        removeCookie(response);
        return "redirect:/";
    }

    public void removeCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie("id", null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
    }


    /**
     * 회원가입
     */
    @PostMapping("/register/create")
    public String userCreate(@Valid RegisterCustomer registerCustomer) {
        Boolean result = customerService.createUser(registerCustomer);
        if (result) {
            return "redirect:/";
        } else {
            return "redirect:/login";
        }
    }
    // thymeleaf로 구현
    // 1. 로그인 페이지로 이동
    // 2. 이메일, 패스워드 입력
        // 3. 유효성 검사.
        // 4. 만약 유효성 검사에 실패한다면 에러 메세지 보여주기
        // 5. 만약 유효성 검사에 성공한다면 index페이지를 보여주는데 이때 회원가입 그리고 로그인이 없어지고, 인증된 사용자기 때문에 로그아웃만 보여줌
        // 6. 타임리프로 인증되지 않은 사용자는 다른 텍스트를 보여주는 기능을 사용, 반대로 인증된 사용자는 로그아웃만 보여주도록 설정
        // 7. 프로필 텍스트에는 user id부분으로 바꿔주기.

    // 3. 로그아웃 버튼을 눌렀을때, 현재 로그인된 정보에서 로그아웃.
//
//    @GetMapping("/login")
//    public String




    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRunTimeException(RuntimeException e) {
        if (e instanceof IllegalArgumentException) {
            return ResponseEntity.ok(
                    ErrorResponse.of("Invaild Input",
                            HttpStatus.BAD_REQUEST.value(),
                            "이메일 또는 비밀번호가 일치하지 않습니다.",
                            "이메일 또는 비밀번호를 확인 해주세요."));

        }else if(e instanceof NullPointerException) {
            return ResponseEntity.ok(
                    ErrorResponse.of("Null",
                            HttpStatus.BAD_REQUEST.value(),
                            "이미 로그아웃 상태 입니다.",
                            ""));
        } else {
                return ResponseEntity.ok(
                        ErrorResponse.of("Failed Access",
                                HttpStatus.BAD_REQUEST.value(),
                                "조회가 실패 하였습니다.",
                                "다시 시도해 주십시오."));
        }
    }

    /**
     * Validation Exception 처리.
     * 형식에 맞춰서 회원가입을 하지 않았을 경우 발생.
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBindException(BindException e) {
        if (e instanceof MethodArgumentNotValidException) {
            log.info(e.getMessage());
            return ResponseEntity.ok(
                    ErrorResponse.of("Failed Validation for argument",
                            HttpStatus.BAD_REQUEST.value(),
                            "값이 올바르게 작성되지 않았습니다. 작성 형식을 확인해주세요.",
                            "회원가입 형식을 확인해주세요."));
        } else {
            // BindException이 다른 경우에 대한 처리
            return ResponseEntity.badRequest().body(
                    ErrorResponse.of("Binding Error",
                            HttpStatus.BAD_REQUEST.value(),
                            "데이터 바인딩 오류가 발생했습니다.",
                            "입력 데이터를 확인해주세요.")
            );
        }
    }

    /**
     * 사용자 ID 또는 Email이 중복 되었을 경우 발생.
     */
    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateKeyException(DuplicateKeyException e) {
        log.info(e.getMessage());
        return ResponseEntity.badRequest().body(ErrorResponse.of("Duplicate Key Error",
                HttpStatus.BAD_REQUEST.value(),
                "사용자 ID 또는 이메일이 중복 되었습니다.",
                "다른 아이디나 이메일로 시도 해주세요."));
    }
}
