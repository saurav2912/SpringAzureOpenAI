package com.saurav.SpringAzureOpenAI;

import jakarta.annotation.PostConstruct;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class VectorDBConfig {

    @Autowired
    private VectorStore vectorStore;

    //@PostConstruct
    public void uploadFiletoVector(){

        TokenTextSplitter splitter = TokenTextSplitter.builder().withKeepSeparator(true).build();

        PagePdfDocumentReader reader = new PagePdfDocumentReader(new ClassPathResource("Ahmed.pdf"));
        List<Document> docs = splitter.split(reader.get());

        PagePdfDocumentReader reader2 = new PagePdfDocumentReader(new ClassPathResource("Allen.pdf"));
        List<Document> docs2 = splitter.split(reader2.get());

        PagePdfDocumentReader reader3 = new PagePdfDocumentReader(new ClassPathResource("Bob.pdf"));
        List<Document> docs3 = splitter.split(reader3.get());

        PagePdfDocumentReader reader4 = new PagePdfDocumentReader(new ClassPathResource("Rohit.pdf"));
        List<Document> docs4 = splitter.split(reader4.get());

        PagePdfDocumentReader reader5 = new PagePdfDocumentReader(new ClassPathResource("Siva.pdf"));
        List<Document> docs5 = splitter.split(reader5.get());
        vectorStore.add(docs);
        vectorStore.add(docs2);
        vectorStore.add(docs3);
        vectorStore.add(docs4);
        vectorStore.add(docs5);

    }
}
