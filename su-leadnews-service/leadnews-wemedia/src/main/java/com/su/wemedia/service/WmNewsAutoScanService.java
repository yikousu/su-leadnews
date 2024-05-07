package com.su.wemedia.service;

import net.sourceforge.tess4j.TesseractException;

import java.io.IOException;

public interface WmNewsAutoScanService {
    public void autoScanWmNews(Integer id) throws TesseractException, IOException;
}
