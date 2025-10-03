package com.goDelivery.goDelivery.config;

import net.sourceforge.tess4j.Tesseract;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TesseractConfig {
    
    @Bean
    public Tesseract tesseract() {
        Tesseract tesseract = new Tesseract();
        // Set the path to the tessdata directory (where language files are stored)
        // You need to have the tessdata directory in your resources folder
        tesseract.setDatapath("./src/main/resources/tessdata");
        tesseract.setLanguage("eng"); // Set the language
        tesseract.setPageSegMode(1);
        tesseract.setOcrEngineMode(1);
        return tesseract;
    }
}
