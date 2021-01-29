package com.qianlima.offline.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.springframework.stereotype.Service;
import org.wltea.analyzer.lucene.IKAnalyzer;

import java.io.StringReader;

@Service
@Slf4j
public class KaFenCiService {


    public String getIkMax(String text) throws Exception{
        StringBuilder stringBuilder = new StringBuilder();
        Analyzer anal=new IKAnalyzer(true);
        //分词
        StringReader reader=new StringReader(text);
        TokenStream tokenStream = anal.tokenStream("", reader);
        CharTermAttribute term=tokenStream.getAttribute(CharTermAttribute.class);
        //遍历分词数据
        tokenStream.reset();
        while(tokenStream.incrementToken()){
            stringBuilder.append(term.toString()+ ",");
        }
        tokenStream.close();
        reader.close();
        return stringBuilder.toString() != null ? stringBuilder.toString().substring(0, stringBuilder.toString().length() - 1) : "";
    }


    public String getIkMin(String text) throws Exception{
        StringBuilder stringBuilder = new StringBuilder();
        Analyzer anal=new IKAnalyzer(false);
        //分词
        StringReader reader=new StringReader(text);
        TokenStream tokenStream = anal.tokenStream("", reader);
        CharTermAttribute term=tokenStream.getAttribute(CharTermAttribute.class);
        //遍历分词数据
        tokenStream.reset();
        while(tokenStream.incrementToken()){
            stringBuilder.append(term.toString()+ ",");
        }
        tokenStream.close();
        reader.close();
        return stringBuilder.toString() != null ? stringBuilder.toString().substring(0, stringBuilder.toString().length() - 1) : "";
    }



}
