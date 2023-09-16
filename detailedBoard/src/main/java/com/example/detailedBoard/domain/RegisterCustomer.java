package com.example.detailedBoard.domain;

// customer 가 가지고 있어야 하는것은
// 일단 데이터베이스 부터 설계를 해야 겟네
// 우선 customer 기본 id 값 auto increment로 하면 회원의 독립적인 key값은 갖게 되는거고
// 회원 이름
// email
// password
// 사는 지역
// 주소지
// 관리자 여부
// 회원가입 날짜
// 게시글 수
// post 의 작성자랑 회원이랑 엮어서 join헤서 게시물 생성할때마다 게시글 수 하나 씩 조인하면 될 것 같은데
// 근데 이때 어떤 게시물을 썼는지도 알아야 하니까 이건 회원에서 필요 없겠다. post에서 관리할 일이지
// 어짜피 누가 어떤 게시물을 썼는지 알고 싶으면 해당 이름으로 된 post값들을 전부 불러오면 되니까 ㅇㅋ

// 데이터 베이스는 일단 다 만들었고
// 그럼 이제 login을 할 수 있는 페이지로 가는걸 controller에서 만들어 주어야 겠네

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterCustomer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;

    @NotBlank
    String userId;
    // unique 값을 저장한 이유는 유일한 값만 저장 되어야 하기 때문이다.
    // unique = true를 하게 된다면, 유일한 값만이 저장된다.
    @Column(unique = true)
    @NotBlank
    @Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+.[A-Za-z]{2,6}$", message = "이메일 형식에 맞지 않습니다.")
    String userEmail;

    @Column(unique = true)
    @NotBlank
//    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\\\d)(?=.*\\\\W).{8,20}$")
    @Size(min = 8, message = "비밀번호는 영문과 특수문자 숫자를 포함하며 8자 이상이어야 합니다.")
    String userPassword;

    // null이 아니어야 하며, 하나 이상의 공백이 아닌 문자를 포함해야 한다.
    @NotBlank
    String userLocation;

    @NotBlank
    String userAddress;
    int admin = 0;
    String registerTime;
    int postCount = 0;
}
