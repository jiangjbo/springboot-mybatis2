package com.hansight.springbootmybatis2.rest.config.enums;

public enum  LanguageEnum {

    CN("zh_cn"),
    EN("en");

    private String language;

    LanguageEnum(String language) {
        this.language = language;
    }

    public String language(){
        return language;
    }

    public static LanguageEnum getEnum(String language){
        for (LanguageEnum languageEnum : LanguageEnum.values()) {
            if (languageEnum.language.equals(language)) {
                return languageEnum;
            }
        }
        return CN;
    }
}
