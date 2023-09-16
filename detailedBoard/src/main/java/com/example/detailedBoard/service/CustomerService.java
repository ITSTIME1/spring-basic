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
    // 크립토 strength 는 default 가 10
    // 따라서 생성자를 그냥 디폴트로 생성하게 된다면 -1 값이 넘어가고
    // -1 값이 넘어가게 된다면 삼항 연산자에 의해서 strength = 10으로 정해짐
    // 만약 strength값이 10~31 이하라면 해당 지정한 strength가 지정됨
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public CustomerService(CustomerRepository customerRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.customerRepository = customerRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }


    /**
     * 고객 정보 생성 만약 아이디가 중복되거나, 이메일이 중복된다면 DuplicateKeyException발생.
     * 그렇지 않다면 암호화 후 디비에 저장.
     * @return true
     */
    public Boolean createUser(RegisterCustomer registerCustomer) {
        if(customerRepository.isUserIDAvailable(registerCustomer.getUserId())
                || customerRepository.isUserEmailAvailable(registerCustomer.getUserEmail())) {
            throw new DuplicateKeyException("사용자 아이디 또는 이메일이 이미 존재합니다.");
        }
        registerCustomer.setUserPassword(bCryptPasswordEncoder.encode(registerCustomer.getUserPassword()));
        return customerRepository.createUser(registerCustomer);
    }

    // 해당 로그인 정보자체를 넘겨줄거기 때문에 반환타입은 LoginCustomer로 작성한다.
    // 비밀번호를 검사하는 항목을 만든다.
    // 따라서 이메일이 존재하고, 패스워드가 일치한다면 해당 유저 정보를 반환하는 로직을 작성.
    // 그럼 해당 유저의 id 번호를 리턴받고
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

}
