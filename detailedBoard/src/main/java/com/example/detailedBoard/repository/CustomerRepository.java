package com.example.detailedBoard.repository;

import com.example.detailedBoard.Interface.CustomerRepositoryInterface;
import com.example.detailedBoard.domain.LoginCustomer;
import com.example.detailedBoard.domain.RegisterCustomer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.security.sasl.AuthenticationException;
import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Repository
public class CustomerRepository implements CustomerRepositoryInterface {

    private final DataSource dataSource;
    private final Logger log = LoggerFactory.getLogger(getClass());

    public CustomerRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * 유저 생성
     */
    @Override
    public Boolean createUser(RegisterCustomer registerCustomer) {
        String sql = "insert into board.customer values(?, ?, ?, ?, ?, ?, ?, ?, ?)";
        ResultSet rs = null;
        try (Connection con = dataSource.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);) {

            // 유저생성
            createCustomer(registerCustomer, pstmt);
            pstmt.executeUpdate();
            rs = pstmt.getGeneratedKeys();

            // 유저를 생성한게 존재한다면 true를 리턴하고, 그렇지 않으면 false를 리턴한다.
            return rs.next();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            if (rs!=null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }

            }
        }
    }

    /**
     * 특정 유저 조회
     */
    @Override
    public LoginCustomer getAnyUser(String email, String password) {
        // email이 일치하는 사용자의 정보만 가지고 오자
        log.info("getAnyUser " + email + " " + password);
        String sql = "select id, userId, email, password from board.customer where email = ? and password = ?";
        ResultSet rs = null;
        try (Connection con = dataSource.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, email);
            pstmt.setString(2, password);
            rs = pstmt.executeQuery();

            // 만약 customer 값이 존재한다면
            // 만약 유저 정보가 존재하지 않는다면
            if (rs.next()) {
                log.info("유저 암호화한 패스워드 DB: " + rs.getString("password"));
                LoginCustomer loginCustomer = new LoginCustomer();
                loginCustomer.setId(rs.getInt("id"));
                loginCustomer.setUserId(rs.getString("userId"));
                loginCustomer.setEmail(rs.getString("email"));
                loginCustomer.setPassword(rs.getString("password"));
                return loginCustomer;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return null;
    }


    /**
     * 특정 유저의 게시물 증가.
     */
    @Override
    public boolean incrementPostCount(String userId) {
        String sql = "update board.customer set postCount = postCount + 1 where userId = ?";
        ResultSet rs = null;
        try (Connection con = dataSource.getConnection();
             PreparedStatement pstmt =  con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)){
            pstmt.setString(1, userId);
            pstmt.executeUpdate();
            rs = pstmt.getGeneratedKeys();
            return rs.next();

        } catch (SQLException e){
            throw new RuntimeException(e);
        } finally {
            if (rs!=null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }

            }
        }
    }

    /**
     * 유저 아이디 중복 여부 검사
     */
    @Override
    public Boolean isUserIDAvailable(String userId) {
        // 만약에 특정 userID가 있는지를 검사해야 되니까
        String sql = "select userId from board.customer where userId = ?";
        ResultSet rs = null;
        try (Connection con = dataSource.getConnection();
        PreparedStatement pstmt =  con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)){
            pstmt.setString(1, userId);
            rs = pstmt.executeQuery();
            // 만약 중복된 값이 있으면 true가 될거고 없으면 false가 될거고
            return rs.next();

        } catch (SQLException e){
            throw new RuntimeException(e);
        } finally {
            if (rs!=null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }

            }
        }
    }

    /**
     * 유저 이메일 중복 여부 검사.
     */
    @Override
    public Boolean isUserEmailAvailable(String userEmail) {
        // 만약에 특정 userID가 있는지를 검사해야 되니까
        String sql = "select email from board.customer where email = ?";
        ResultSet rs = null;
        try (Connection con = dataSource.getConnection();
             PreparedStatement pstmt =  con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)){
            pstmt.setString(1, userEmail);
            rs = pstmt.executeQuery();
            // 만약 중복된 값이 있으면 true가 될거고 없으면 false가 될거고
            return rs.next();

        } catch (SQLException e){
            throw new RuntimeException(e);
        } finally {
            if (rs!=null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }

            }
        }
    }

    /**
     * 유저 패스워드 검사
     */
    @Override
    public String isUserPasswordAvailable(String userEmail) {
        String sql = "select * from board.customer where email = ?";
        ResultSet rs = null;
        try (Connection con = dataSource.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)){
            // 패스워드를 바인딩해준다.
            pstmt.setString(1, userEmail);
            rs = pstmt.executeQuery();
            // 만약 중복된 값이 있으면 true가 될거고 없으면 false가 될거고
            // 여기서 authentication이 나는데 음 결국 일치하는 테이블을 찾을 수 없다는거 같은데
            if(rs.next()) {
                return rs.getString("password");
            } else {
                throw new AuthenticationException();
            }
        } catch (SQLException | AuthenticationException e){
            throw new RuntimeException(e);
        } finally {
            if (rs!=null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }

            }
        }
    }

    /**
     * 유저 생성 메인 로직.
     */
    private void createCustomer(RegisterCustomer registerCustomer, PreparedStatement pstmt) throws SQLException {
        pstmt.setInt(1, registerCustomer.getId());
        pstmt.setString(2, registerCustomer.getUserId());
        pstmt.setString(3, registerCustomer.getUserEmail());
        pstmt.setString(4, registerCustomer.getUserPassword());
        pstmt.setString(5, registerCustomer.getUserLocation());
        pstmt.setString(6, registerCustomer.getUserAddress());
        pstmt.setInt(7, registerCustomer.getAdmin());

        // 시간설정
        LocalDateTime nowDateTime = LocalDateTime.now();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formatDateTime = dateTimeFormatter.format(nowDateTime);
        registerCustomer.setRegisterTime(formatDateTime);

        pstmt.setString(8, registerCustomer.getRegisterTime());
        pstmt.setInt(9, registerCustomer.getPostCount());
    }
}
