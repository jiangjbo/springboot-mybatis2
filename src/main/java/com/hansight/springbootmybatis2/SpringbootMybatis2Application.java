package com.hansight.springbootmybatis2;

import com.hansight.spider.cfg.Configuration;
import com.hansight.springbootmybatis2.common.elasticsearch.util.SpringUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.elasticsearch.rest.RestClientAutoConfiguration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@SpringBootApplication(exclude = RestClientAutoConfiguration.class)
public class SpringbootMybatis2Application {

	@RequestMapping("/")
	String index(){
		return "spring boot " ;
	}

	public static void main(String[] args) {
		SpringUtil.setAc(SpringApplication.run(SpringbootMybatis2Application.class, args));
	}

}

