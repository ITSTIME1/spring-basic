package com.example.detailedBoard.repository;

import com.example.detailedBoard.DetailedBoardApplication;
import com.example.detailedBoard.domain.Post;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ContextConfiguration(classes = DetailedBoardApplication.class)
@ActiveProfiles("test")
class PostRepositoryTest {
    private final DataSource dataSource;

    @Autowired
    public PostRepositoryTest(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Test
    @DisplayName("PostRepository 프로필 적용 테스트.")
    void testProfileTest() {
        Connection con = null;
        try {
            con = dataSource.getConnection();
            if (con == null || con.isClosed()) {
                System.out.println("DB 연결 되지 않음.");
            } else {
                System.out.println("DB 정상 연결.");
                System.out.println("DB 이름 : " + con.getMetaData().getDatabaseProductName());
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery("Show tables");
                // board_test 스키마 안에 현재 접근하고 있는 table을 가져온다. 예상 값은 post_table이어야함.
                rs.next();
                assertEquals("post_test", rs.getString(1), "fail");
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // 계정을 생성
    @Test
    @DisplayName("PostRepository 게시글 생성 테스트.")
    void createPost() {
        String insertPostQuery = "insert into board_test.post_test values(?, ?, ?, ?, ?, ?, ?)";
        int previousID = -1;
        // 이전 id값을 가지고 오기 위해서

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            // Connection Pool로부터 Connection객체를 할당받음.
            con = dataSource.getConnection();
            // 미리 이전 키 값을 받아놓고 이후 test post 객체가 제대로 생성이 되었다면 키값을 비교한다.
            String selectPreviousIdQuery = "select max(id) from board_test.post_test";
            pstmt = con.prepareStatement(selectPreviousIdQuery);
            rs = pstmt.executeQuery();
            // next를 하지 않아서 움직이지 않았나보네
            if(rs.last()) {
                previousID = rs.getInt(1);
            }

            // sql을 미리 컴파일 해서 가지고 있음.
            pstmt = con.prepareStatement(insertPostQuery, Statement.RETURN_GENERATED_KEYS);

            // 테스트 게시글을 생성해서 값을 임의부여.
            Post post = new Post();

            // 임의의 이름
            UUID uuid = UUID.randomUUID();
            String sampleName = uuid + "test";

            // 현재시간
            LocalDateTime nowDateTime = LocalDateTime.now();
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String formatDateTime = dateTimeFormatter.format(nowDateTime);

            post.setUsername(sampleName);
            post.setTitle("test1 title");
            post.setContent("test1 content");
            post.setCurrentDatetime(formatDateTime);

            pstmt.setInt(1, post.getId());
            pstmt.setString(2, sampleName);
            pstmt.setString(3, post.getTitle());
            pstmt.setString(4, post.getContent());
            pstmt.setString(5, post.getCurrentDatetime());
            pstmt.setInt(6, post.getViewCount());
            pstmt.setInt(7, post.getLikeCount());

            // value값이 맵핑된 상태로 쿼리문 수행.
            pstmt.executeUpdate();
            rs = pstmt.getGeneratedKeys();

            if(rs.last()) {
                // 마지막 행의 값을 잘 반환 했다면 true를 반환하기 때문에
                // 데이터가 잘 저장된걸 확인할 수 있다.
                // 여기서 추가적인 체크를 해야할까?
                // 이전 마지막 id 값에서 + 1 증가한 값이라면 제대로 생성이 되었다는 것을 알 수 있다.
                assertEquals(previousID + 1, rs.getInt(1));
            }

        } catch (Exception e) {
            throw new IllegalStateException(e);
        } finally {
            // 주어진 connection객체를 수행하고 연결을 닫는다.
            DataSourceUtils.releaseConnection(con, dataSource);
        }
    }

    @Test
    @DisplayName("PostRepository 특정 게시글 불러오기 테스트")
    void readAnyPost() {
        String selectAnyPostQuery = "select * from board_test.post_Test where id = ?";

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            // Connection Pool로부터 Connection객체를 할당받음.
            con = dataSource.getConnection();

            // sql을 미리 컴파일 해서 가지고 있음.
            pstmt = con.prepareStatement(selectAnyPostQuery);

            // 50번째 데이터를 가지고옴.
            pstmt.setInt(1, 50);

            // 해당 키값을 정상적으로 반환하는지 확인.
            rs = pstmt.executeQuery();

            // 키를 찾았다면 해당 id값이 존재하는 것이기 때문에
            // username을 비교해서 제대로 가져 왔는지 테스트 한다.
            if(rs.next()){
                // 50번째 username을 미리 예상값으로 등록해 놓은뒤 해당 값이랑 일치 하는지 확인.
                String username = "29f1d831-1989-4a6e-a5eb-b09cf623b31atest";
                assertEquals(username, rs.getString(2));
            }

        } catch (Exception e) {
            throw new IllegalStateException(e);
        } finally {
            // 주어진 connection객체를 수행하고 연결을 닫는다.
            DataSourceUtils.releaseConnection(con, dataSource);
        }
    }

    @Test
    @DisplayName("PostRepository 모든 게시물 불러오기 테스트")
    void readAllPost() {
        // 모든 포스트를 전부 조회 해야 한다는 것.
        // 따라서 해당 목적은 List<Post> 리스트에 포스트가 null아 아니어야 한다는것.
        String selectAllPostQuery = "select * from board_test.post_test";
        List<Post> postList = new ArrayList<>();
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            con = dataSource.getConnection();
            pstmt = con.prepareStatement(selectAllPostQuery);
            rs = pstmt.executeQuery();
            // 모든 게시물들의 행을 조회하기 때문에 행을 조회하면서 Post객체를 만들어 list에 넣어준다.
            while(rs.next()) {
                Post post = new Post();
                post.setId(rs.getInt(1));
                post.setUsername(rs.getString("username"));
                post.setTitle(rs.getString("title"));
                post.setContent(rs.getString("content"));
                post.setCurrentDatetime(rs.getString("currentDatetime"));
                post.setViewCount(rs.getInt(6));
                post.setLikeCount(rs.getInt(7));
                postList.add(post);
            }

        } catch (Exception e) {
            throw new IllegalStateException(e);
        } finally {
            // 검증
            // postList가 null아 아니어야 하며, 개수는 0보다 커야 함.
            assertNotNull(postList);
            assertTrue(postList.size() > 0);
            DataSourceUtils.releaseConnection(con, dataSource);
        }
    }

    @Test
    void delete() {
    }

    @Test
    void viewCountUpdate() {
    }

    @Test
    void likeCountUpdate() {
    }
}