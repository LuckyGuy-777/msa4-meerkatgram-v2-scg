package com.msa4meerkatgramv2scg;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class Msa4MeerkatgramV2ScgApplication {

    public static void main(String[] args) {
        SpringApplication.run(Msa4MeerkatgramV2ScgApplication.class, args);
    }

}

// 메인 클래스에, jdk 설정하라는, 노란색 팝업이 뜬다면,
// jdk 설정 -> 기존에 맞는 자바버전 클릭 -> 안잡힌다면, jdk 다운로드를 새로 다운받으면 된다. ( 어떤이유로, jdk가 삭제되거나, 인식을 못한경우가 있을것임.)