package com.qianlima.offline;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
		System.out.println(getShuchu());
	}

	private static StringBuffer getShuchu(){
		StringBuffer s = new StringBuffer();
		s.append("===================================================================");
		s.append("===============================h_poc项目启动成功=============================");
		s.append("===================================================================");
		return s;
	}
}
