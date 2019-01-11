package com.hansight.springbootmybatis2.common.multilingual;

import com.hansight.spider.cfg.Configuration;
import com.hansight.springbootmybatis2.rest.config.enums.LanguageEnum;
import com.hansight.springbootmybatis2.rest.config.service.SystemConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(value=1)
public class MultilingualInit implements CommandLineRunner {

    @Autowired
    SystemConfigService systemConfigService;

    @Value("${spring.datasource.url}")
    private String url;
    @Override
    public void run(String... args) throws Exception {
        LanguageEnum language = systemConfigService.systemLanguage();
        Configuration.instance().registerDatasourceUrl(url);
        Configuration.instance().registerSystemLanguage(language.language());

    }
}
