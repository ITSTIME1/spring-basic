package com.example.board.service;

import com.example.board.domain.Board;
import com.example.board.respository.BoardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;
import java.util.UUID;

@Service
public class BoardService {

    @Autowired
    private BoardRepository boardRepository;

    // 해당 메소드는이제
    // 비즈니스 로직을 처리할 책임을 부여받게 되고
    // 비즈니스 로직을 처리할 대상은 회원의 요청이기 때문에
    // 회원이 요청한 데이터를 기반으로 요청을 처리하게 된다.
    // 그렇다면 회원의 요청을 파라미터로 받아서 처리하게 되면 되는 것이다.
    // 이 메서드의 목적은 board 라는 데이터를 받았을때
    // 디비에 해당 게시물 작성 내용을 저장하는것을 목표로 하는것이다.
    // 그럼 이 해당 비즈니스 로직 처리하는 메소드는
    // 디비에 접근할 수 있는 repository의 기능을 이용해야 된다. 디비와 가장 밀접한 로직을 가지고 있는것이 repository의 역할이기 때문이고
    // 따라서 이 service 비즈니스로직을 담당하고 있는 메소드는 repository를 의존하고 있다는 것을 알 수 있으며 의존하고 있기 때문에
    // 해당 repository가 담고 있는 디비 관련 기능들을 사용할 수 있는 것이다.
    // 그니까 글을 작성하면
    // 글이 작성완료 했다는 메세지를 보내줄 수 있고
    // 만약 글이 작성이 제대로 되지 않았다면 어떠한 오류에 의해서
    // 그러한 사항들을 클라이언트측에 알려줄 수 있지.

    // 따라서 클라이언트에서 message를 받고 있다가
    public void postWrite(Board board, MultipartFile file) throws Exception{
        String projectPath =System.getProperty("user.dir") + "/src/main/resources/static/files";

        // 식별자 랜덤으로 식별자를 만들어준다.
        UUID uuid = UUID.randomUUID();

        String fileName = uuid + "_" + file.getOriginalFilename();
        File saveFile = new File(projectPath, fileName);
        file.transferTo(saveFile);
        board.setFilename(fileName);
        board.setFilepath("/files/" + fileName);
        boardRepository.save(board);
    }


    // 이제 두번째 비즈니스 로직을 처리할건데
    // 게시글을 전체 조회할 수 있는 기능을 처리해야 하기 때문에
    // 해당 게시글을 전부 담은 list형식으로 Board데이터들을 전부 반환하게 될 것이도

    public Page<Board> findPost(Pageable pageable) {
        // 타입 객체를 전부 리턴하기 때문에 해당 타입을 List로 감싸 리턴하게 된다.
        return boardRepository.findAll(pageable);
    }

    // 해당 비즈니스 로직의 목적은 단일 게시글에 대한 정보를 디비에서 리턴받고 싶은 것이기 때문에
    // 해당 id값만을 Controller에서 받아서 해당 게시글을 디비에서 불러와 Board엔티티 객체로 만들어주고
    // 해당 Board 객체를 반환한다.
    public Board findAnyPost(Integer id) {
        return boardRepository.findById(id).get();
    }

    // 게시글을 삭제하는 로직같은 경우는 해당 id값을db 에서 지우는 기능이기 때문에
    // 해당 게시글 id값을 받아줘서 해당 id를 디비에서 지워주는 역할을 해주게끔 구현해주면 된다.
    // 따라서 Controller에서 게시글과 관련된 id값을 가지고 있어야하며
    // 그 id값을 가지고 비즈니스 로직에서는 db에 있는 값을 지우게 되는 것이다.
    // 해당 기능 같은 경우 반환값이 별도로 필요로 되어지지 않는 디비내에서만 지워지면 되기 때문에
    // 이후에 나중에 복잡한 처리문이 추가가 되게 된다면 성공적으로 지워졌는지에 대한 내용들을 추가적으로 보낼 수 있을 것 같다.

    public void deletePost(Integer id) {
        boardRepository.deleteById(id);
    }


}
