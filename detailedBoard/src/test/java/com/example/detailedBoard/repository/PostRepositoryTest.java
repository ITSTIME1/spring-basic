package com.example.detailedBoard.repository;

import com.example.detailedBoard.DetailedBoardApplication;
import com.example.detailedBoard.domain.Post;
import jakarta.transaction.Transactional;
import org.aspectj.lang.annotation.Before;
import org.junit.jupiter.api.*;
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

// 통합테스트

/**
 * 개선해야 될 점 재사용 문제 중복된 코드가 많기 때문에 재사용 문제가 돋보인다.
 * db연결관리도 최적화를 진행해 볼 수 있다.
 * prepared statement도 재사용이 가능하기 때문에 개선할 수 있다.
 * resultset 관리
 * 공통 메소드 추출.
 *
 * 위와 관련된 내용들을 포스팅하면 되겠다. before after로 중복된 코드들을 개선할 수 있는 방법으로.
 */
@SpringBootTest
@ActiveProfiles("test")
class PostRepositoryTest {
    private final DataSource dataSource;
    // Connection 객체는 매번 메소드마다 사용하기 때문에 하나만 만들어두고 가져다 사용하는 방법으로 쓰면
    // 매번 Connection 객체를 메소드마다 만들지 않아도 마지막에 Connection을 반환해주는 형태로 만들어줄 수 있다.
    // 이를 이용하면 메모리, 속도 측면에서 향상되길 기대할 수 있다.
    // 왜냐하면 매번 connection = datasource.getConnection() 을 이용해서 불필요한 connection을 지속적으로 가지고 오기 때문이다.
    // 어떠한 의미있는 데이터를 가지고 오는 것이 아닌 데이터베이스와 연결할 수 있는 매개체만 가지고 오는 것이다보니, 연결된 매개체는 하나만 사용해서
    // connection을 돌려가며 사용할 수 있다. 따라서 불필요한 보일러코드의 최소화를 진행할 수 있다..
    private Connection connection;
    private PreparedStatement pstmt;
    private ResultSet rs;
    private LocalDateTime nowDateTime;
    private DateTimeFormatter dateTimeFormatter;

    @Autowired
    public PostRepositoryTest(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    // beforeall 와, afterall을 사용해서 더 최적화 시켜볼 수 있을것 같다.
    // beforeall이라는 것은 해당 클래스가 테스트 될때 딱 한번만 실행 되는 것들을 명시한다.
    // 가령 데이터 source로부터 connection을 얻어오는 방법은 매 메소드 마다 실행할 필요가 없다.
    // 하나의 connection을 가지고 공유하면 되니까
    // 결국 모든 테스트가 끝난 후에는 connection을 반환하기만 하면 된다.
    // 따라서 매 메소드마다 connection객체를 생성할 필요가 존재하지 않음을 알 수 있다.
    // 1. 따라서 공통된 사항은 connection객체를 하나만 생성해서 공유하는것.
    // 2. preparestatement를 사용하지 메소드는 변경 요함.
    // 3. 테스트 데이터를 수동으로 설정해서 사용하고 있지만, 이걸 자동으로 만들고 초기화까지 진행 해주면 편할 것 같다.
    // 4. createPost같은 경우 데이터베이스와의 연결에 대한 책임도 가지고 있으면서 동시에 테스트 데이터까지 만들고 있다.
    // 이를 분리하여 테스트 데이터를 생성하는 것과 데이터베이스 연결 두가지로 나눠서 바라보면 좋을것 같다.

    // 5. 테스트 데이터를 초기화 하는게 필요하다. @AfterEach를 사용해서 각 메소드가 수행된 후 초기화 작업을 진행해줄게 있다면 진행해준다.
    // 6. 메소드 명칭 생각하기.

    @BeforeEach
    void beforeEach() {
        // 동시성 문제로 인해서 각 메소드가 끝나면 connection을 닫아주고
        // 다른 메소드가 시작할때 다시 connection을 얻어온다.
        try {
            connection = dataSource.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @AfterEach
    void afterEach() {
        // 매 메소드마다 데이터를 초기화 하는것이 좋을것 같다.
        // 메모리 누수가 발생할 수 있기 때문에 리소스를 명시적으로 해제한다.
        // 왜냐하면 GC가 자동적으로 해당 객체를 더 이상 사용하지 않을때 일반 변수는 메모리를 회수해 가지만, rs, pstmt는 자동적으로 회수하지 않기 때문이다.
        // 또한 해당 메소드를 afterEach로 구현해둔 이유는 매번 메소드가 호출 될때 마다 어떠한 테스트 메소드에서는 rs를 사용하지 않을수도 있고, pstmt를 사용하지 않을수도 있기 때문에
        // 그러한 가능성까지 고려하여 더 이상 사용하지 않는다면 명시적으로 닫아주는게 좋은 선택이라고 생각한다.
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        if (pstmt != null) {
            try {
                pstmt.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

    }

    @Test
    @DisplayName("PostRepository 프로필 적용 테스트.")
    void testProfileTest() {
        try {
            if (connection == null || connection.isClosed()) {
                System.out.println("DB 연결 되지 않음.");
            } else {
                System.out.println("DB 정상 연결.");
                System.out.println("DB 이름 : " + connection.getMetaData().getDatabaseProductName());
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
        String selectPreviousIdQuery = "select max(id) from board_test.post_test";
        int previousID = -1;

        try {
            // 미리 이전 키 값을 받아놓고 이후 test post 객체가 제대로 생성이 되었다면 키값을 비교한다.
            pstmt = connection.prepareStatement(selectPreviousIdQuery);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                previousID = rs.getInt(1);
            }
            // sql을 미리 컴파일 해서 가지고 있음.
            pstmt = connection.prepareStatement(insertPostQuery, Statement.RETURN_GENERATED_KEYS);

            Post post = new Post();

            // 현재시간
            nowDateTime = LocalDateTime.now();
            dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String formatDateTime = dateTimeFormatter.format(nowDateTime);

            // 테스트 이름 설정
            String testName = "testName" + (int) (Math.random() * 100);
            String testTitle = "testTitle" + (int) (Math.random() * 100);
            String testContent = "testContent" + (int) (Math.random() * 100);

            post.setUsername(testName);
            post.setTitle(testTitle);
            post.setContent(testContent);
            post.setCurrentDatetime(formatDateTime);

            pstmt.setInt(1, post.getId());
            pstmt.setString(2, post.getUsername());
            pstmt.setString(3, post.getTitle());
            pstmt.setString(4, post.getContent());
            pstmt.setString(5, post.getCurrentDatetime());
            pstmt.setInt(6, post.getViewCount());
            pstmt.setInt(7, post.getLikeCount());

            // value값이 맵핑된 상태로 쿼리문 수행.
            pstmt.executeUpdate();
            rs = pstmt.getGeneratedKeys();

            if (rs.next()) {
                assertEquals(previousID + 1, rs.getInt(1));
                assertNotNull(post.getUsername());
                assertNotNull(post.getContent());
                assertNotNull(post.getCurrentDatetime());
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("PostRepository 특정 게시글 불러오기 테스트")
    void readAnyPost() {
        String selectAnyPostQuery = "select username from board_test.post_Test where id = ?";
        String username = "29f1d831-1989-4a6e-a5eb-b09cf623b31atest";

        try {
            // sql을 미리 컴파일 해서 가지고 있음.
            pstmt = connection.prepareStatement(selectAnyPostQuery);

            // 50번째 데이터를 가지고옴.
            pstmt.setInt(1, 50);

            // 해당 키값을 정상적으로 반환하는지 확인.
            rs = pstmt.executeQuery();

            // 키를 찾았다면 해당 id값이 존재하는 것이기 때문에
            // username을 비교해서 제대로 가져 왔는지 테스트 한다.
            if (rs.next()) {
                // 50번째 username을 미리 예상값으로 등록해 놓은뒤 해당 값이랑 일치 하는지 확인.
                assertEquals(username, rs.getString("username"));
            }

        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    @DisplayName("PostRepository 모든 게시물 불러오기 테스트")
    void readAllPost() {
        String selectAllPostQuery = "select * from board_test.post_test";
        List<Post> postList = new ArrayList<>();

        try {
            pstmt = connection.prepareStatement(selectAllPostQuery);
            rs = pstmt.executeQuery();

            // 모든 게시물의 내용 가져오기
            while (rs.next()) {
                Post post = new Post();
                post.setId(rs.getInt("id"));
                post.setUsername(rs.getString("username"));
                post.setTitle(rs.getString("title"));
                post.setContent(rs.getString("content"));
                post.setCurrentDatetime(rs.getString("currentDatetime"));
                post.setViewCount(rs.getInt("viewCount"));
                post.setLikeCount(rs.getInt("likeCount"));
                postList.add(post);
            }

            // 객체가 null이 아니어야 하고, post의 개수는 0보다 커야함
            assertNotNull(postList);
            assertTrue(postList.size() > 0);

        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    // 이건 회원가입 기능이 완성되고 나서
    // 해당 게시글을 삭제하려고 할때 그게 해당 소유자의 게시물인지 확인하는 작업이 이루어져야 하고
    // 게시글을 삭제한다는 요청이 들어왔을 경우에는 해당 게시글의 id값을 삭제하는 식으로 진행하면 될 것 같음.
    @Test
    void delete() {
    }

    @Test
    @DisplayName("PostRepository 특정 게시물 조회수 증가 테스트.")
    void viewCountUpdate() {
        String getViewCountQuery = "select viewCount from board_test.post_test where id = ?";
        String increViewCountQuery = "update board_test.post_test set viewCount = viewCount + 1 where id = ?";
        int preViewCount = -1;
        int nextViewCount = -1;

        try {
            // 쿼리를 컴파일 해준뒤
            getViewCount(getViewCountQuery);
            // id값 조회
            if (rs.next()) {
                // 증가 하기전 viewCount 저장
                preViewCount = rs.getInt(1);

                try {
                    // viewCount 증가
                    pstmt = connection.prepareStatement(increViewCountQuery);
                    pstmt.setInt(1, 50);
                    pstmt.executeUpdate();
                    // 현재 viewCount 가져오기
                    // 쿼리를 컴파일 해준뒤
                    getViewCount(getViewCountQuery);
                    if(rs.next()) {
                        nextViewCount = rs.getInt(1);
                    }

                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
            assertEquals(preViewCount + 1, nextViewCount);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }




    // 이 기능도 마찬가지로 사실 회원여부에 따라서 달라져야 한다.
    // 만약 한번이라도 이 게시물에 눌렀던 기록이 남아 있다면
    // 다시 클릭 했을때는 해당 게시물의 기록에서 제외해 주어야 하고, 제외된 만큼 빠져야 하기 때문에 likeCount 개수를 업데이트 한다.
    // 보아하니 조회수도 그렇고 좋아요도 그렇고 사용자로그 방식, 세션 또는 쿠키 방식, 사용자 계정 시스템 방식 이정도가 있는것 같네.
    // 어떻게 해볼 수 있을까? 조회수 자체가 조회한 사람에게만 보이는게 아닌 모든 사람에게 다 보여주어야 하기 때문에 몇명이 조회 했는지는 이렇게 cout를 올려주어서 보여주면 되지만
    // 만약 어떤 로그정보가 필요하다고 한다면 사용자를 역추적 할 수 있는 방법도 존재하지 않을까? 그렇게 하는 방식이 쿠키나 세션 사용자 계정 방식일거 같은데.
    @Test
    @DisplayName("PostRepository 특정 게시물 좋아요 증가 테스트.")
    void likeCountUpdate() {
    }

    private void getViewCount(String getViewCountQuery) throws SQLException {
        pstmt = connection.prepareStatement(getViewCountQuery);
        // 50번째 id값을 기준으로 해서 50번째의 viewCount를 증가시키기 전의 값을 가지고오고
        pstmt.setInt(1, 50);
        rs = pstmt.executeQuery();
    }

}