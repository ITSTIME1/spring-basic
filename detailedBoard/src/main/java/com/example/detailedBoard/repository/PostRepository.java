package com.example.detailedBoard.repository;

import com.example.detailedBoard.Interface.PostRepositoryInterface;
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
    // 로그 수집을 목적으로 로깅을 사용.
    private final Logger log = LoggerFactory.getLogger(getClass());


    public PostRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }


    // post가 생성될때 발생될 수 있는 에러가 무엇인지 생각해보자.
    // 우선 게시글을 dto로 받아오게 되니 해당 dao 객체를 실제 데이터베이스에서 저장하는데 SQLException이 발생할 수 있다.
    // 따라서 데이터베이스에 저장할때 문제가 발생될 수 있다.
    // 또한 네트워크 문제로 인해서 문제가 발생될 수 있다.
        // 이런 경우 특정 네트워크 횟수를 지정해서 지정한 만큼 재시도하여 재시도를 했음에도 불구하고 네트워크 문제 때문에 해결이 안된다면
        // 다른 에러를 넘겨줄 수 있다.
        // 제목, 내용 둘 중 하나라도 null이면 안되기 때문에 왜? title과 Content의 반환 타입은 String이기 때문에
        // 값이 들어오지 않는다면 null이며 SQLException을 발생시킨다.

    @Override
    public Post createPost(Post post) {
        String sql = "insert into post values(?, ?, ?, ?, ?, ?, ?)";
        ResultSet rs = null;
        try (Connection con = dataSource.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, post.getId());
            // 데이터베이스에 입력하게 될 시간 설정.
            LocalDateTime nowDateTime = LocalDateTime.now();
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String formatDateTime = dateTimeFormatter.format(nowDateTime);
            post.setCurrentDatetime(formatDateTime);

            // pstmt value 값 매핑.
            pstmt.setString(2, "taesun");
            pstmt.setString(3, post.getTitle());
            pstmt.setString(4, post.getContent());
            pstmt.setString(5, post.getCurrentDatetime());
            pstmt.setInt(6, post.getViewCount());
            pstmt.setInt(7, post.getLikeCount());
            // 이쪽에서 문제가 생기게 된다면 어짜피 SQLException을 터트릴거고
            // runtimeException으로 감싸서 클라이언트로전송.
            pstmt.executeUpdate();

            // insert문을 조회하고 난 뒤 해당 생성된 키 값을 반환
            rs = pstmt.getGeneratedKeys();

            // resultSet object를 리턴하게 될테니까
            // insert 가 잘 들어갔다면
            if(rs.next()) {
                // post가 정상적으로 들어 왔을때
                // 생성된 게시물로 이동할 수 있게끔 해주는게 좋지 않을까?
                // 만약 생성된 게시물이 제대로 오지 않았을 경우는 error페이지를 보여주면 되니까
                post.setId(rs.getInt(1));
                return post;
            }

        } catch (SQLException e) {
            // 데이터베이스에 문제가 생겼다면 Runtime을 보낸다.
            throw new RuntimeException(e);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    // 자원을 중지할때 문제가 생겼다면 runtimeException을 보낸다.
                    throw new RuntimeException(e);
                }
            }
        }
        // post가 보내져야 하는데 보내지지 못했다면
        throw new RuntimeException("Failed to retrieve generated ID");
    }

    // readPost
    @Override
    public Post readAnyPost(Integer id) {
        String sql = "select * from post where id = ?";
        Post post = new Post();
        ResultSet rs = null;
        try (Connection con = dataSource.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql);) {

            pstmt.setInt(1, id);
            // 쿼리에 의해서 생성되어진 ResultSet 객체를 리턴한다.
            // 이거에 관해서 블로그 포스팅 해보자.
            // resultset에 관해서!
            rs = pstmt.executeQuery();

            if (rs.next()) {
                log.info("해당 id" + id);
                post.setId(rs.getInt("id"));
                post.setUsername(rs.getString("username"));
                post.setTitle(rs.getString("title"));
                post.setContent(rs.getString("content"));
                post.setLikeCount(rs.getInt("likeCount"));
                post.setViewCount(rs.getInt("viewCount"));
                post.setCurrentDatetime(rs.getString("currentDatetime"));
            }
        }  catch (SQLException e) {
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
        return post;
    }

    // db에 있는 내용들을 전부다 가져와서 tr에 뿌려주어야 겠지
    @Override
    public List<Post> readAllPost() {
        // 우선 DB에 있는 내용들을 다 가지고 오려면 해당 db에서 모든 객체들을 다 가져와야 하니까
        // sql문을 작성하고 해당 sql문을 통해서 모든 데이터를 가져오는 방법으로 해보자.
        // sql문을 작성해서 모든 데이터를 받고
        String sql = "select * from post";
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            con = dataSource.getConnection();
            pstmt = con.prepareStatement(sql);
            rs = pstmt.executeQuery();

            List<Post> postList = new ArrayList<>();

            // 모든 데이터를 DB에서 꺼내와서 post객체를 만들어 준다음 postList로 넘김.
            while (rs.next()) {
                // rs.next()를 통해서 row cursor를 하나씩 옮기는거지
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
        } catch (Exception e) {
            throw new IllegalStateException(e);
        } finally {
            // 주어진 connection객체를 수행하고 연결을 닫는다.
            DataSourceUtils.releaseConnection(con, dataSource);
        }
    }

    @Override
    public void delete() {

    }

    // 조회수는 해당 게시글에 접속했을때 하나씩 올라야하니까
    @Override
    public void viewCountUpdate(Integer id) {
        String sql = "update post set viewCount = viewCount + 1 where id = ?";

        // try-with-resource 문을 통해서 자동으로 자원을 해제 할 수 있도록 해준다.
        try(Connection con = dataSource.getConnection();
            PreparedStatement pstmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            // sql exception일 발생되면 runtime exception으로 포장해서 넘겨주면 된다.
            // sql exception은 데이터베이스 장애나 네트워크 장애가 일어나는 경우이고, 결국 service layer나 controller layer에서
            // 해결할 수도 없다.
            // 따라서 클라이언트에게 해당 사실을 알려준다.
            throw new RuntimeException(e);
        }

    }

    // 좋아요 개수 올리기.
    @Override
    public void likeCountUpdate(Integer id) {
        // 간단한 update 쿼리문인데도 불구하고 시간이 30005ms나 걸린다. 왜그럴까?
        // 1ms = 0.001 기 때문에
        // 30005ms = 30005 * 0.001s 이 되기 때문에
        // 약 30.005초 정도 걸린다.
        // 간단한 쿼리문인데도 30초나 걸리나?
        // 보아하니 Connection poll에 대해서 이해를 해야 하는 것 같다.
        String sql = "update post set likeCount = likeCount + 1 where id = ?";
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        // 좋아요를 올려준다.
        try {
            con = dataSource.getConnection();
            pstmt = con.prepareStatement(sql);
            // 10초를 타임아웃으로 설정해둔뒤
            // 10초가 넘어간다면 SQLtimeException이 발생하도록 한다.
            pstmt.setQueryTimeout(10);
            pstmt.setInt(1, id);
            rs = pstmt.executeQuery();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        } finally {
            // 주어진 connection객체를 수행하고 연결을 닫는다.
            DataSourceUtils.releaseConnection(con, dataSource);
        }
    }
}
