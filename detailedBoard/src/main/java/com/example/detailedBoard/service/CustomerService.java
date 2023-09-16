package com.example.detailedBoard.service;

import com.example.detailedBoard.domain.LoginCustomer;
import com.example.detailedBoard.domain.RegisterCustomer;
import com.example.detailedBoard.repository.CustomerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.security.sasl.AuthenticationException;

@Service
public class CustomerService{
    private final CustomerRepository customerRepository;
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public CustomerService(CustomerRepository customerRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.customerRepository = customerRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }


    /**
     * 유저 생성
     */
    public Boolean createUser(RegisterCustomer registerCustomer) {
        if(customerRepository.isUserIDAvailable(registerCustomer.getUserId())
                || customerRepository.isUserEmailAvailable(registerCustomer.getUserEmail())) {
            throw new DuplicateKeyException("사용자 아이디 또는 이메일이 이미 존재합니다.");
        }
        registerCustomer.setUserPassword(bCryptPasswordEncoder.encode(registerCustomer.getUserPassword()));
        return customerRepository.createUser(registerCustomer);
    }

    /**
     * 유저 로그인
     */
    public LoginCustomer loginUser(String email, String password) {
        // password를 암호화해서 저장해둔뒤, 암호화한 패스워드와 검사한다.
        log.info("유저 패스워드 : " + password);
        // 만약 이메일이 있고
        if (customerRepository.isUserEmailAvailable(email)) {
            String dbPassword = customerRepository.isUserPasswordAvailable(email);
            // db에서 받아온 passWord와 유저가 입력한 평문과 일치한지 확인.
            if (bCryptPasswordEncoder.matches(password, dbPassword)) {
                return customerRepository.getAnyUser(email, dbPassword);
            }
        }
        throw new IllegalArgumentException("이메일이나 비밀번호가 일치하지 않습니다.");
    }

    /**
     * 유저 게시물 개수 증가.
     */
    public void incrementUserPostCount(String userId) {
        customerRepository.incrementPostCount(userId);
    }

}
