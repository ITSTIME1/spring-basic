package com.example.detailedBoard.Interface;

import com.example.detailedBoard.domain.LoginCustomer;
import com.example.detailedBoard.domain.RegisterCustomer;

public interface CustomerRepositoryInterface {
    // 회원가입
    Boolean createUser(RegisterCustomer registerCustomer);

    // 유저 정보 가져오기
    LoginCustomer getAnyUser(String email, String password);
    // 아이디 변경
    // 비밀번호 변경

    /**
     * 이메일 변경 같은 경우는 이메일 중복 여부를 판단해서, 이메일이 이미 있는 경우는 다른 이메일을 사용해서 가입하게끔
     */
    // 이메일 변경

    // 아이디 중복확인
    Boolean isUserIDAvailable(String userId);
    // 이메일 중복확인
    Boolean isUserEmailAvailable(String userEmail);

    // 패스워드 중복확인
    String isUserPasswordAvailable(String userEmail);
}
