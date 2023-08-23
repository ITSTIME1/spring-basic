package com.example.detailedBoard.repository;

import com.example.detailedBoard.domain.Post;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.UUID;

@Repository
public class PostRepository implements PostRepositoryInterface{
    private final DataSource dataSource;

    public PostRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void create(Post post) {
        String sql = "insert into post values(?, ?, ?, ?, ?, ?)";
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        // 우선적으로 JDBC라고 함은
        // 관계형 데이터베이스와의 연결을 도와주는 브릿지 같은 역할을 하게 되는
        // API다. 해당 API를 통해서 자바에서 직접 쿼리를 작성해서 날리는걸 도와주게 된다.
        // 또한 이 JDBC API는 JDBC Driver를 통해서 구현되어 졌는데
        // 이러한 JDBC Driver는 JDBC interface들의 집합인데
        // JDBC Driver가 가지고 있는 이러한 집합에는 JDBC를 불러들이는 것 그리고 결과를 자바 어플리케이션으로 반환해주는 내용이 있다.
        // 따라서 자바에서 사용되어지는 이러한 JDBC API는 아래와 같은 메인 객체들을 포함하여 개발자가 이용하게 되는데
        // 1. DataSource Object 는 easblish to connection 연결을 위해서 사용되어지는 객체다.
        // DriverManager로도 연결을 할 수 있지만 DataSource 객체를 활용해서 연결객체를 얻는것이 더 선호되어지는 방법이라고 한다.
        // Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/myDb", "user1", "pass")
        // 위와 같은 식으로도 DriverManager를 통해 DB와의 연결 객체를 얻을 수 있다.
        // 위 코드를 보면 왜 Db와 연결되는 객체라는 건지 명확하게 알 수 있을 것이다.
        // 아마 대부분의 개발은 application.properties 에서 명시를 해두었기 때문에
        // DriverManager로 직접 객체를 사용하지 않고 DataSource를 얻는 방법을 많이 사용하낟.
        // 그럼 이러한 connection을 얻고 나면 Db와 연결되었기 때문에 con이라는 객체를 가지고
        // DB에 쿼리를 수행할 수 있는 준비가 되어 있다.
        // 하지만 이를 실질적으로 준비하게 하려면 Statement 타입의 종류들 중 하나를 사용하게 되는데
        // 이때 prepareStatement를 사용하게 된다. 왜 prePareStatement를 사용할까
        // 우선 prePareStatement는 사전 컴파일된 SQL 문을 가지고 있다. 어떻게 가지고 있냐면
        // PrePareStatement 말그대로 사전 준비 상태를 뜻한다. 현재 pstmt = com.prepareStatement를 사용해서
        // 미리 작성해두었던 sql문을 파라미터로 넘겨주고 있다. 두번재 파라미터는 일단 넘겨둔다.
        // 그렇다면 이 PrepareStatement객체는 미리 우리가 작성해두었던 sql문을 사전 컴파일해둔다.
        // 그리고 question mark 매개변수를 바인딩한다고 표현하기도 하는데
        // 어쨌든 question mark를 통해서 사전 컴파일된 sql에 값만을 설정해 sql문을 완성할 수 있다.
        // 곰곰히 생각해보면 preparedStatement를 사용하는 이유는 명확하다.
        // 첫번째로 보안적인 이슈를 생각해볼 수 있다. 흔히 sql이라고 하면 sql injection을 떠올려 볼 수 있는데
        // sql injection이라고 하는것은 비정상적인 방법 혹은 쿼리를 통해서 데이터베이스를 탈취하는 행위를 뜻한다.
        // 보통 해커들이 데이터베이스를 침투하려고 할때 sql injection을 수행하게 되고, 이것을 수행하는 이유는
        // sql 문에서 항상 참인 값을 이용해 탈취하기 때문이다.
        // 다시 돌아와 값을 셋팅하고 나서 excuteUpdate() 메소드를 사용해서 쿼리를 수행하게 된다.
        // 이때 실질적으로 쿼리를 날리게 된것이다.
        // 여기서 잠깐 정리해보면 전체적인 흐름으로 보자 datbase 연결 정보를 얻어주고
        // 해당 연결정보를 가지고 sql문을 사전에 컴파일하여 객체를 생성해 둔뒤
        // 그 객체를 이용해 매개변수 바인딩을 하여 값을 주고
        // 쿼리문을 실행 하는 순서대로 했다.
        // 그럼 얘기하지 않았던 Statement.RETURN_GENERATED_KEYs 를보면
        // 이는 상수이다. 즉 자동으로 생성되는 키를 반환하도록 지정하는 키다.
        // 해당 상수에 대한 설명이 있는데

        // The constant indicating that generated keys should be made
        //     * available for retrieval.
        // 문장이 조금 애매한것 같다. 이러한 상수는 생성 되어진 키들은 검색을 위해 사용가능해야 한다라는 건데
        // 음... 문장을 해석해 봐도 느낌이 영.. 그래서 조금 의역을 해보자면 생성된 키를 검색을 한다는건
        // 자동으로 생성되어진 키들이 있을때 검색이 가능한 키들이어야 한다는걸 의미한다?
        // 위 문장을 보면 좀 어색하지만 그냥 생성되어진 키를 반환하라고 정해둔 상수라고 보면된다.
        // 따라서 해당 생성되어진 키를 얻을 수 있다.
        //


        // 그렇게 DataSource를 통해서 DB연결관련 객체를 얻게 되었다면 이를 활용하여 이제 SQL Statement를 만들 수 있게 된다.
        // Statement라고 하는것은 SQL문을 실행하기 위한 것인데.


        try {
            con = dataSource.getConnection();

            pstmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            UUID uuid = UUID.randomUUID();
            String sampleName = uuid + "good";

            pstmt.setInt(1, post.getId());
            pstmt.setString(2, sampleName);
            pstmt.setString(3, post.getTitle());
            pstmt.setString(4, post.getContent());
            pstmt.setString(5, post.getCurrentDatetime());
            pstmt.setString(6, post.getCurrentDatetime());


            pstmt.executeUpdate();
            rs = pstmt.getGeneratedKeys();

            if (rs.next()) {
                System.out.println("디비 저장 성공");
            } else {
                throw new SQLException("디비 저장 실패");
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        } finally {
            // 주어진 connection객체를 수행하고 연결을 닫는다.
            DataSourceUtils.releaseConnection(con, dataSource);
        }
    }


    @Override
    public void read() {

    }

    @Override
    public void readAll() {

    }

    @Override
    public void delete() {

    }

    @Override
    public void update() {

    }
}
