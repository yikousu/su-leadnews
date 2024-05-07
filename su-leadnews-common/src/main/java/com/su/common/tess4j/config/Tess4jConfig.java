package com.su.common.tess4j.config;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Tess4jConfig {
    @Value("${tess4j.datapath:null}")
    private String datapath;
    @Value("${tess4j.language:null}")
    private String language;

    @Bean
    public ITesseract iTesseract() {
        ITesseract iTesseract = new Tesseract();
        iTesseract.setDatapath(datapath);
        iTesseract.setLanguage(language);
        return iTesseract;
    }

}
