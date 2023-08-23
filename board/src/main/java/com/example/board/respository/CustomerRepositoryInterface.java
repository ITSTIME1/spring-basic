package com.example.board.respository;

import com.example.board.domain.Customer;

public interface CustomerRepositoryInterface {
    // insert 는 Customer 정보를 저장하기 위한 메소드이고
    // Customer 엔티티를 파라미터로 받아서 데이터베이스에 저장한다.
    public void insert(Customer customer);


    // 해당 메소드는 custId를 받아서 해당 커스톰 아이디를 찾는 메소드가 된다.
    public Customer findByCustomerId(int custId);

}
