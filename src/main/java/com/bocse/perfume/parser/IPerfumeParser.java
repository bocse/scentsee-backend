package com.bocse.perfume.parser;

import com.bocse.perfume.data.Perfume;

import java.io.IOException;
import java.util.Map;

public interface IPerfumeParser {
    Map<String, String> parseBrands() throws IOException, InterruptedException;

    Map<String, String> parserBrand(String url) throws IOException, InterruptedException;

    Map<String, String> parseBrandPage(String url, Integer page) throws IOException, InterruptedException;

    Perfume parsePerfumeDocument(String url) throws IOException, InterruptedException;

    boolean getPerfumePictures(Perfume perfume, String path) throws IOException, InterruptedException;
}
