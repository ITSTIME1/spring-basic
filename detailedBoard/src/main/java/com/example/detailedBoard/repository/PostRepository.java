package com.example.detailedBoard.repository;

import com.example.detailedBoard.Interface.PostRepositoryInterface;
import com.example.detailedBoard.domain.LoginCustomer;
import com.example.detailedBoard.domain.Post;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Repository
// lombok 라이브러리가 제공해주는 로깅 어노테이션
//@Slf4j
public class PostRepository implements PostRepositoryInterface {
    private final DataSource dataSource;
    private final Logger log = LoggerFactory.getLogger(getClass());


    public PostRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }


    /**
     * 게시물 생성
     */
    @Override
    public Post createPost(Post post, LoginCustomer user) {
        // 이제 username 에다가는 사용자 정보를 넣어주자.
        String sql = "insert into post values(?, ?, ?, ?, ?, ?, ?)";
        ResultSet rs = null;
        Connection con = null; // Connection 변수를 밖으로 빼줍니다.

        try {
            con = dataSource.getConnection();
            con.setAutoCommit(false);

            try (PreparedStatement pstmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setInt(1, post.getId());
                // 데이터베이스에 입력하게 될 시간 설정.
                LocalDateTime nowDateTime = LocalDateTime.now();
                DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                String formatDateTime = dateTimeFormatter.format(nowDateTime);
                post.setCurrentDatetime(formatDateTime);

                // pstmt value 값 매핑.
                // 사용자 정보를 받아야하는데
                pstmt.setString(2, user.getUserId());
                pstmt.setString(3, post.getTitle());
                pstmt.setString(4, post.getContent());
                pstmt.setString(5, post.getCurrentDatetime());
                pstmt.setInt(6, post.getViewCount());
                pstmt.setInt(7, post.getLikeCount());

                pstmt.executeUpdate();

                // insert문을 조회하고 난 뒤 해당 생성된 키 값을 반환
                rs = pstmt.getGeneratedKeys();
                con.commit();
                if (rs.next()) {
                    post.setId(rs.getInt(1));
                }
            } catch (SQLException e) {
                con.rollback();
                throw new RuntimeException(e);
            }

            return post;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get connection");
        } finally {
            // 자원을 닫습니다.
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    throw new RuntimeException("Failed to close ResultSet", e);
                }
            }
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                    throw new RuntimeException("Failed to close Connection", e);
                }
            }
        }
    }


    /**
     * 특정 게시물 읽기
     */
    @Override
    public Post readAnyPost(Integer id) {
        String sql = "select * from post where id = ?";
        ResultSet rs = null;
        try (Connection con = dataSource.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql);) {

            pstmt.setInt(1, id);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                Post post = new Post();
                post.setId(rs.getInt("id"));
                post.setUsername(rs.getString("username"));
                post.setTitle(rs.getString("title"));
                post.setContent(rs.getString("content"));
                post.setLikeCount(rs.getInt("likeCount"));
                post.setViewCount(rs.getInt("viewCount"));
                post.setCurrentDatetime(rs.getString("currentDatetime"));
                return post;
            } else {
                throw new NullPointerException();
            }
        }  catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    throw new RuntimeException("Failed to close resultset");
                }
            }
        }

    }

    /**
     * 게시물 전체 읽기
     */
    @Override
    public List<Post> readAllPost() {
        String sql = "select * from post";

        try (Connection con = dataSource.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            List<Post> postList = new ArrayList<>();

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

            return postList;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to read all post");
        }
    }


    @Override
    public void delete() {

    }

    /**
     * 조회수 업데이트
     */
    @Override
    public void viewCountUpdate(Integer id) {
        String sql = "update post set viewCount = viewCount + 1 where id = ?";
        Connection con = null;

        try {
            con = dataSource.getConnection();
            con.setAutoCommit(false); // 트랜잭션 시작

            try (PreparedStatement pstmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setInt(1, id);
                pstmt.executeUpdate();

                con.commit(); // 트랜잭션 커밋
            } catch (SQLException e) {
                con.rollback(); // 예외 발생 시 롤백
                throw new RuntimeException("Rollback failed", e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get connection");
        } finally {
            try {
                if (con != null) {
                    con.close();
                }
            } catch (SQLException e) {
                throw new RuntimeException("Failed to close connection");
            }
        }
    }

    /**
     * 게시물 좋아요 여부 체크
     */
    @Override
    public void isLiked(Integer userId, Integer id) {
        // 우선적으로 확인해봐야지
        String sql = "select * from board.likes where userId = ? and postId = ?";
        ResultSet rs = null;
        try(Connection con = dataSource.getConnection();
            PreparedStatement pstmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            // 1 은 userId 그리고 2는 postId
            pstmt.setInt(1, userId);
            pstmt.setInt(2, id);
            rs = pstmt.executeQuery();

            // 이게 true를 반환하게 되면 userId 가 post_Id를 좋아요를 누른거고, 그렇지 않으면 누르지 않은거니까
            if(rs.next()) {
                // 좋아요 삭제
                likeDelete(userId, id);
            } else {
                // 좋아요를 추가해주고
                likeInsert(userId, id);
            }
            // 좋아요를 반영해준다.
            likeUpdate(id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            if(rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    throw new RuntimeException("Failed to close resultset");
                }
            }
        }
    }

    /**
     * post의 likeCount를 업데이트한다.
     */
    private void likeUpdate(Integer id) {
        // join을 이용해야 할거 같은데
        // postid가 같은 것들을 찾아서 count해서 likeCount에 반영하면 될거 같은데
        String sql = "update board.post set likeCount = " +
                "(select count(postId) from board.likes where postId = ?) where id = ?";

        Connection con = null;
        try {
            con = dataSource.getConnection();
            // 트랜잭션 시작
            con.setAutoCommit(false);

            try (PreparedStatement pstmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                // 1 은 userId 그리고 2는 postId
                pstmt.setInt(1, id);
                pstmt.setInt(2, id);
                pstmt.executeUpdate();
                con.commit();
            } catch (SQLException e) {
                // 업데이트 진행하다 문제가 발생 되었다면, 롤백진행
                con.rollback();
                throw new RuntimeException(e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get onnection");
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                    throw new RuntimeException("Failed to close connection");
                }
            }
        }
    }

    /**
     * likes 테이블에 좋아요 반영
     */
    private void likeInsert (Integer userId, Integer id) {
        String sql = "insert into board.likes value (?, ?)";
        Connection con = null;
        try {
            con = dataSource.getConnection();
            con.setAutoCommit(false);
            try(PreparedStatement pstmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                // 1 은 userId 그리고 2는 postId
                pstmt.setInt(1, userId);
                pstmt.setInt(2, id);
                pstmt.executeUpdate();
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw new RuntimeException(e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get connection");
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                    throw new RuntimeException("Failed to close conneciton");
                }
            }
        }

    }

    /**
     * like 테이블에서 해당 정보삭제
     */
    private void likeDelete(Integer userId, Integer id) {
        String sql = "delete from board.likes where userId = ? and postId = ?";
        Connection con = null;
        try {
            con = dataSource.getConnection();
            con.setAutoCommit(false);
            try(PreparedStatement pstmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                // 1 은 userId 그리고 2는 postId
                pstmt.setInt(1, userId);
                pstmt.setInt(2, id);
                pstmt.executeUpdate();
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw new RuntimeException(e);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                    throw new RuntimeException("Failed to close connection");
                }
            }
        }
    }
}
