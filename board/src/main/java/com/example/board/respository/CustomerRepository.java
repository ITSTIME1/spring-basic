package com.example.board.respository;

import com.example.board.domain.Customer;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class CustomerRepository implements CustomerRepositoryInterface{
    private DataSource dataSource;


    // 의존성 주입 Spring container 에서 주입을 해주게 된다.
    public CustomerRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // 회원을 저장하는 메소드
    @Override
    public void insert(Customer customer) {
        // 여기에서 이제 회원 객체가 들어오게 되면
        // 회원 객체를 가지고 나서 데이터베이스에 넣어주어야 하기 때문에
        // 해당 customer 테이블에다가 cust_id, name, age를 넣어주게 되는 쿼리문이다.
        // 이때 ? 를 사용하는 이유는 sql에서 매개변수 바인딩을 지원하기 때문이다.
        // 따라서 동적으로 값을 할당할 수 있으며, 보안성능을 높일 수 있다.
        // 그러면 데이터베이스는 해당 쿼리를 컴파일하게 되면 동적으로 값만 바꿀 수 있기 때문에
        // 최적화가 끝난 이후 ? 만 넣게 되면 쿼리문을 전체를 다시 컴파일할 이유가 없어진다.
        // 또한 SQL injection을 방지할 수 있다.
        // SQL injection은 사용자 주입방식인데
        // 사용자가 직접 value에 해당하는 값들을 주입하는 방식이라고 한다.
        // 하지만 이러한 방식이 무서운 이유는 URL 쿼리스트링에 직접적으로 어떤 키값으로
        // 사용자의 입력값이 들어가는지 알 수 있다면 그 URL 쿼리스트링을 임의로 조작하여
        // 항상 참이 되게 하는 값을 넣는다던지 비정상적인 구문을 실행하려고 할때 이용이 가능해지고
        // 이는 데이터베이스의 구조를 유추해볼 수 있는 실마리를 제공해줌과 동시에 보안이 급격히 낮아지게된다.
        // 따라서 이를 해결하기 위해서
        // 1. 쿼리문 검증 (미리 설정한 특수문자들이 들어왔을때 차단하는 방법이다.)
        // 2. error message 노출금지. 에러 발생시 따로 처리를 해주지 않는다면 쿼리문과 함께 여러 내용이 보여질 수 있기 때문에
        // 이를 이용하여 injection이 들어올 수 있다. 따라서 별도로 처리해주는 작업이 필요하다.
        // 3. prepared statement 구문 사용
        // sql 문을 미리 pre-compiled 되어진 상태로 만들면 sql에서 매개변수 바인딩을 통해
        // 해당 값을 동적으로 할당할 수 있게 만든다음에 ? 들어갈 값만을 동적으로 변경해 sql문을 실행하는 것이다.
        // 이는 미리 컴파일된 sql문을 외부에서 변경할 수 없고, ? 들어갈 값만을 넣게 된다. 하지만
        // ? 들어갈 값은 단순 문자 취급하기 때문에 sql인젝션을 방지할 수 있다.
        // 따라서 preStatement구문을 사용하는 이유다.
        String sql = "INSERT INTO CUSTOMER " +
                "(CUST_ID, NAME, AGE) VALUES (?, ?, ?)";
        // db와 연동될 Connection을 하나 만들어주고
        Connection conn = null;

        try{
            // get db connection
            conn = dataSource.getConnection();
            // ready into database table
            // prepareStatement를 사용해서
            // prepareStatement는 SQl을 하드코딩하지 않다고하더라도
            // 파라미터화 시켜 실행시킬 수 있는 것이다.
            // 아래 코드에서도 볼 수 있듯이 디비의 연결정보 객체를 가지고
            // prepareStatement 메소드를 실행시켜 그 안으로 작성했던 sql 문을 넣어주었다. 파라미터로
            // 이렇게 되면 이후에 있을 customer getUserid 라던지 name이라던지
            // 해당 값들을 하드코딩 하지 않고서 파라미터 인덱스를 활용하여 디비에 주입할 수 있게 된다.
            // 이후 해당 값들을 전부 디비에 반영할 준비가 되면
            // ps.excuteUpdate() 를 사용해서 sql문을 여기서부터 디비로 날리게 된다.
            // 이때부터 반영이 된다라고 볼 수 있다.
            // 즉 위 sql문에서는 CUSTOMER 테이블에 고객정보를 넣으려고 하는데
            // 이때 VALUES 값들을 정의해주어야 한다.
            // VALUES 값들은 앞서 Ccustomer객체가 받아올 정보에 담겨 있고
            // 이 정보들을 values에 매칭시켜주어야 한다.
            // 따라서 해당 ? ? ? 에 파라미터방식으로 접근이 가능하게 해줄 수 있는 객체가 바로
            // PreparedStatement다
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, customer.getCustId());
            ps.setString(2, customer.getName());
            ps.setInt(3, customer.getAge());
            // db를 업데이트
            // 오케이 여기서부터 다시 해보자
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    // 회원을 찾는 메소드
    @Override
    public Customer findByCustomerId(int custId) {
        return null;
    }
}
