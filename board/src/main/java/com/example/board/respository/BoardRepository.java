package com.example.board.respository;

import com.example.board.domain.Board;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


// respository 라는것은 디비와의 연동성을 강하게 가지고 있는 클래스파일이다.
// 그렇기 때문에 디비와 관련되어 있는 로직들이 실행되게 되고, Service와의 의존관계를 통해서
// 비즈니스 로직을 처리할때 실질적으로 데이터베이스에 CRUD를 할 수 있는 기능들을 제공해준다.
// service와 repository가 분리되어 있는 이유를 설명할 수 있게 되었으며, service는 말그대로 비즈니스 로직을 처리한다라는 것에 목적을 두고 있으며
// 비즈니스 로직을 처리하기 위한 세부디테일을 들고 있는건 repository에서 행하게 되는 것이다.
// 그렇게 된다면 비즈니스 로직을 처리하는데 집중할 수 있게 되며, repository는 디비에 관련된 로직을 처리하는데 더 집중 할 수 있게 된다.
// 그러므로 업무의 분업화가 이루어지며 책임의 범위를 느슨하게 만들어주었다고 볼 수 있다.


@Repository
public interface BoardRepository extends JpaRepository<Board,Integer> {

}
