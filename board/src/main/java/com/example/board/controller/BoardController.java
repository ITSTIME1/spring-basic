package com.example.board.controller;

import com.example.board.domain.Board;
import com.example.board.service.BoardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

// spring 이 controller라는걸 알 수 있게.
// @Controller 의 역할은 스프링에서는 일반적으로 autodetecting을 지원해서
// dispatcher가 클래스패스 안에서 해당 클래스가 Component 클래스인지 아닌지를 해당 어노테이션으로 판단하며
// 빈에 자동으로 등록해주는 역할을 하게 된다.
// 따라서 앞서 Spring MVC 역할에서 Controller의 역할은 View 와 Model의 브릿지 역할을 하는 존재였고
// 그러한 존재여부를 알려주는 어노테이션이라고 이해하면 좋을 것 같다.
// You can define annotated controller beans explicitly, using a standard Spring bean definition in the dispatcher's context
// 위와 같이 명시적으로 빈에다가 annotated 한 controller를 명시할 수도 있다 dispatcher's context 안에서
// 이는 spring-context에 직접적으로 등록을 할 수 있다는 얘기가 된다.

// @GetMapping 이라는건 특정 HTTP get 요청을 받는 핸들러라는 걸 명시하기 위한 어노테이션이며
// 해당 어노테이션을 해당 url로 들어온 요청을 처리하기 위한 메소드에 지정한다.


// 전반적인 흐름을 보자면 dispatcher servelt 이 앞에서 모든 요청을 다 받은다음에
// 해당 요청중에서 handller mapping이 Controller를 찾게되고 매핑된 Controller를 찾았기 때문에
// 아니 맵핑된 컨트롤러가 존재한다면 매핑된 컨트롤러를 handling adpater를 통해서 실행을 요청한다.
// 이후 해당 해당 컨트롤러는 브릿지 역할을 수행하게 되며 전반적인 처리를 맡게 되게 되는데
// 이때 세부적으로 비즈니스 로직을 처리하기 위한 Service에 책임을 전가하며
// 그렇게 요청된 데이터를 구조적으로 처리하여 반환받게 된 후 HandlerAdapter가 ModelAndView 객체로 변환하여
// 응답에 해당하는 정보 혹은 데이터를 포함한 객체를 반환하게 된다.



// DispatcherServelt 이라고 하는 front Controller는 클라이언트의 요청을 받게되고
// 해당 요청이 왔을때 Handlermapping을 통해서 요청에대한 검사를 요청하게 되고
// 적절한 hadnlerexception chain을 형성하게 됩니다.
// 그리고 나서 dispatcher servelet은 그 핸들러를 실행하게되게 된다(올바른 요청에대한 핸들러를 찾게 되었을때)
// 따라서 이때 요청에대한 처리를 하기 위해서 적절한 무언가를 찾아야하는데 그때 찾게 되는게 바로 @Controller가 되고
// 해당 컨트롤러를 찾게되면 HandlerAdapter에 의해서 실행되어지게 된다.
//When a request comes in, the DispatcherServlet will hand it over to the handler mapping to let it inspect the request and come up with an appropriate HandlerExecutionChain. Then the DispatcherServlet will execute the handler and interceptors in the chain (if any).
@Controller
public class BoardController {
    @Autowired
    private BoardService boardService;

    @GetMapping("/board")
    public String boardWriteForm() {
        return "boardwrite";
    }


    // 해당 포스트 맵핑에 대해서 어떤 파라미터를 받을건지
    // 자 이때 해당 URL로 들어온 요청을 파라미터를 통해서 값을 받을건데
    // 이처럼 파라미터의 이름이 요청한 key값과 일치하지 않으면 내용을 볼 수가 없는걸 알 수 있다.
    // 그렇기 때문에 클라이언트에서 지정한 key값을 토대로 받아야 한다는 것을 알 수 있다.
    // lombok을 통해서 자주사용되는 getter/setter 등과 같은 것들을 자바 컴파일 시점에 자동으로 생성하여
    // 클래스 파일에 작성하지 않아도 어노테이션을 정의하는 것만으로도 사용할 수 있게 만든 라이브러리.
    @PostMapping("/board/writepro")
    public String boardWritePro(Board board, MultipartFile file) throws Exception{
        // 해당 데이터가 넘어왔나 콘솔에 찍어보면
        // 데이터를 Board 엔티티를 통해 객체화 되어진 것을
        // 하나의 데이터로 보고 그 데이터 객체를 이제 서비스 로직을 실행하는 서비스에게 책임을 전가시켜야 하기 때문에
        // BoardService에게 해당 데이터를 처리해달라고 값을 넘겨준다.
        System.out.print(board.getContent());
        // 하지만 이처럼 boardService는 해당 클래스에서 어떤 건지 알 수 없다. 따라서 이때도 DI 의존성 주입을 통해서
        // 해당 서비스를 넣어주는데 @Autowired를 통해서 의존성 주입을 Spring 레벨에서 해주게 되고
        // 그렇게 되면 이제 boardService를 알게 되기 때문에
        // 주입이 된 상태에서 해당 비즈니스 로직을 수행할 수 있게 되는 것이다.
        boardService.postWrite(board, file);
        return "";
    }


    // 그럼이제 게시글을 작성해서 디비에 저장하는 작업까지 해봤으니까
    // 해당 게시글들을 전부 불러오는 작업도 해봐야겠지
    // 그렇게 하기 위해서 게시글을 불러온다는 것은 디비에 있는 전체적인 게시글들을 불러온다는 것과 같기 때문에 여기에서는
    // 모든 Post게시글을 전부 가져올 수 있도록 만약 프론트와 협업하게 된다면 어떠한 버튼을 누르거나 어떠한 페이지로 이동을 했을경우 수동적으로
    // 해당 게시글 내용들이 나타날 수 있게 연동되어 지겠지만 여기에서는 직접 URL을 입력하고 접속했을때 해당 게시물들이 전부 보여지는 형태로 구현을 할 예정이다.

    // 따라서 어떠한 URL로 들어오는지에 대해서 우선적으로 명시를 해주어야 하며 이는 클라이언트가 해당 URL을 통해 접속을 요청했을때
    // 보여지게 하기 위함이다.
    // 구체적인 구현은 boardlist html을 보여주기 위함이니까 해당 html을 반환할 수 있도록 한다.

    // 자 그러면 이제 findAllPost는 해당 boardlist와 밀접한 연관성이 있으며
    // 요청한 데이터를 html파일에 반환을 해주어야 하기 때문에
    // 우선적으로 respository에서 해당 데이터를 전부 가져오는 역할을 해야한다.
    @GetMapping("/board/list")
    public String findAllPost(Model model, @PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {

        // 해당 데이터를 Model을 통해서 html에 적용해줄건데
        // model 객체를 이용하여 list라는 이름으로 boardService 에서 처리한 비즈니스 로직 여기에서는 모든 게시글들을 찾는것이 되며
        // 해당 게시글을 찾는 기능이 동작이 되고 나면 findPost는 List<Board> Board객체를 List타입으로 반환하게 되며 리스트로 되어 있기 때문에
        // Board에 대한 내용들이 전부 담겨서 반환되게 된다.
        Page<Board> list = boardService.findPost(pageable);
        // 현재 페이지를 알 수 있고
        // 아래 로직을 보면 현재 페이지의 번호를 얻고
        // 이야 이걸 설명을 안한다고?
        // nowPage가 7페이지라고 한다면
        // 말그대로 시작페이지는 블럭에 가장 처음 부분을 말하는거기 때문에
        // 블럭의 처음 부분이 몇개씩 보여질건지를 설정하는거자나
        // 그럼 가정이 필요해 나는 10개씩 보여주고 싶다.
        // 그래서 현재에서 start까지는 이만큼 보여주고 그럼 4개의 페이지가 보여지니까
        // 10개중 나머지 6개만 보여지면 되는거자나
        // 하지만 내 현제피이지도 보여져야 하기 때문에 내 페이지를 제외하고
        // +5만큼 해서 나머지 부분을 보여지게 한다음에
        // 나의 페이지 까지 보여주면 10페이지가 되는거자나
        // 이걸 설명을안한다고?
        int nowPage = list.getPageable().getPageNumber();
        int startPage = Math.max(nowPage - 4, 1);
        int endPage = Math.min(nowPage + 5, list.getTotalPages());
        model.addAttribute("list", list);


        return "boardlist";
    }


    // 이제 특정 게시물을 클릭했을때
    // 해당 특정 게시물을 보여줄 수 있는 뷰를 만들어보고
    // 그럼 뷰에다가 특정 id값을 가지고 있는 값을 활용해서
    // 비즈니스 로직을 처리할건데 이때 id값을 받았으니까 해당 게시물이 몇번째 게시물인지 알거고
    // 그러면 해당 게시물을 찾기 위해서 사용되는 값은 id값이라는걸 알 수 있다.
    // 따라서 해당 Id값으로 디비에서 찾아와 보여주는 작업을 진행한다.
    // 그렇게 하기 위해서 repository에서 현재 어떠한 쿼리 메소드도 존재하지 않기 때문에
    // 우선적으로 해당 게시물을 findany()값을 활용하여 비즈니스 로직에서 찾아서 해당 Board객체를 리턴하면

    // 서비스 객체가 해당 id에 맞는 Board 엔티티를 이용해 값을 넘겨주게 된거곡
    // 그 데이터를 이제 뷰에 적용시키면된다.

    @GetMapping("/board/anypost")
    public String findAnyPost(Model model, Integer id) {
        // 그럼 비즈니스 로직이 처리되고 나서 즉 findAnyPost에서 단일 게시글을 받아와
        // html에 뿌려주게 된다.
        // 이때 Integer id 라는 파라미터는 웹에서 어떤 게시글을 클릭했을때
        // 번호를 주게 되면 해당 번호를 파라미터로 받게 되고
        // 그럼 파라미터를 통해서 게시글을 가져오는 것이기 때문에
        // Get방식을 사용하게 되며 앞서 Post 같은 경우에는 어떠한 데이터를 입력하길 원할때 사용하게 된다.
        // 결국 데이터를 저장할 목적을 지니고 있지 않은데 Post를 사용할 필요가 없지 않은가.
        // 해당 데이터를 받아오기만 하면 되니까.
        model.addAttribute("anyBoard", boardService.findAnyPost(id));
        return "boardanypost";
    }


    // 이제 게시글을 삭제하는 기능이 필요로 되어지는데
    // 게시글을 삭제하려면 버튼을 눌렀을때 게시글을 삭제할거기 때문에
    // 해당 게시글을 삭제하기 위해서 /board/delete 라는 곳으로 요청을 할거고
    // 요청을 하게 되면 게시글을 삭제하기 위한 서비스 로직이 동작되며
    // 게시글을 삭제하고 나서는 게시글 목록으로 이동하는 로직을 작성해본다.


    @GetMapping("/board/delete")
    public String deletePost(Integer id) {
        // 마찬가지로 여기서 id값을 받아서 처리를 시작해준다.
        // deletePost기 때문에 게시글을 지워주는 역할을 해야 하니까
        boardService.deletePost(id);
        return "redirect:/board/list";
    }

    // 그러면 이제 게시글 수정이 필요할때
    // 게시글 수정을 위한 기능들이 필요할거 아니야?
    // 게시글 수정은 눌렀을때 해당 게시글의 내용들이
    // write한 곳에 작성이 되어져야 한다는걸 의미하지.
    // 따라서 수정기능을 눌렀을때 해당 게시물의 제목과 내용이 input, textarea에 입력이 되어져야 한다는 걸 알 수 있어
    // 그걸 구현하기 위해서 수정 버튼을 눌렀을때
    // 수정 요청을 받을 /board/modify를 정의하고
    // 해당 modify url로 이동을 하게 될때
    // 게시글의 id를 받게 될거고 그 id를 받아서 데이터베이스에서 해당 내용을 끌어온다음에
    // 그 내용을 model에 담아서 수정 페이지에다가 넘겨줄거야

    // 이때 PathVariable이라는 어노테이션을 사용할건데
    // 이는 쿼리스트링의 형태에서 ?id = 1 이러한 형태에서
    // 쿼리스트링의 키값을 감춰주고 값만이 path형태로 작성되어진다 하여 이러한 어노테이션을 사용한다.
    // 이러면 어떤 값에 뭐가 들어가는지 감출 수 있다.
    @GetMapping("/board/modify/{id}")
    public String modifyPost(@PathVariable("id") Integer id, Model model) {
        // 모델객체를 만들어서 우선적으로 비즈니스 로직을 처리해주고
        // 그렇다는건 데이터베이스에서 해당 id값을 가져와주고
        model.addAttribute("modify", boardService.findAnyPost(id));
        return "boardmodify";
    }


    // 이제 게시글을 수정하기 위해서 path를 받아주어야 하지
    // 마찬가지로 해당 Post에 대한 id를 받아주어야 하고
    // 수정을 하는 쿼리를 짜야되는데
    @PostMapping("/board/update/{id}")
    public String updatePost(@PathVariable("id") Integer id, Board board, MultipartFile file) throws Exception {
        System.out.println(id);
        Board boardTemp = boardService.findAnyPost(id);
        System.out.println(boardTemp.getContent());
        boardTemp.setTitle(board.getTitle());
        boardTemp.setContent(board.getContent());

        boardService.postWrite(boardTemp, file);
        return "redirect:/board/list";
    }
}