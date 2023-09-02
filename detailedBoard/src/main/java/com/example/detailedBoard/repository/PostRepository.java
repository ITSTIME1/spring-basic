package com.example.detailedBoard.repository;

import com.example.detailedBoard.domain.Post;
import com.example.detailedBoard.service.PostService;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import javax.swing.text.html.Option;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class PostRepository implements PostRepositoryInterface {
    private final DataSource dataSource;

    public PostRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void create(Post post) {
        String sql = "insert into post values(?, ?, ?, ?, ?, ?, ?)";
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

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
            pstmt.setInt(6, post.getViewCount());
            pstmt.setInt(7, post.getViewCount());


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


    // readPost
    @Override
    public Post read(Integer id) {
        String sql = "select * from post where id = ?";
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            con = dataSource.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, id);
            rs = pstmt.executeQuery();

            // 존재한다면
            if (rs.next()) {
                Post post = new Post();
                post.setId(rs.getInt("id"));
                post.setTitle(rs.getString("title"));
                post.setContent(rs.getString("content"));
                post.setLikeCount(rs.getInt("likeCount"));
                post.setViewCount(rs.getInt("viewCount"));
                post.setCurrentDatetime(rs.getString("currentDatetime"));
                return post;
            }


        } catch (Exception e) {
            throw new IllegalStateException(e);
        } finally {
            // 주어진 connection객체를 수행하고 연결을 닫는다.
            DataSourceUtils.releaseConnection(con, dataSource);
        }
        return null;
    }

    // db에 있는 내용들을 전부다 가져와서 tr에 뿌려주어야 겠지
    @Override
    public List<Post> readAll() {
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
