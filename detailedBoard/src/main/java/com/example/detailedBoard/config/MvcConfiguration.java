package com.example.detailedBoard.config;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


// @Configuration 속성은 Spring Bean에 수동으로 등록하기 위해서 표현하게 되는데
// 해당 어노테이션을 추가함으로써 Spring Bean에 등록할 수 있게 된다.
// 그러면 이제 IOC 제어의 역전 개념에서 해당 Bean 객체들을 관리하는 IOC container에서 이를 관리하게 된다.
// 따라서 아래는 외부 templates 정적파일들을 사용하기 위해서 classpath와 static을 등록해준다.
@Configuration
public class MvcConfiguration implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // TODO [resources >> static >> 리소스 연결]
        //WebMvcConfigurer.super.addResourceHandlers(registry);
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/templates/", "classpath:/static/");
    }
}
