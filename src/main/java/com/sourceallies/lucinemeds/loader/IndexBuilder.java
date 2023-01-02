package com.sourceallies.lucinemeds.loader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
@Profile("index")
public class IndexBuilder implements ApplicationRunner {
    private static Logger logger = LoggerFactory.getLogger(IndexBuilder.class);

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Path datasetFile = Path.of("drug-ndc-0001-of-0001.json");
        Path dataDirectory = Path.of("data");
        logger.info("Building Lucene index using dataset: {}", datasetFile.toAbsolutePath());
        NDCDataset dataset = readDataset(datasetFile);
        writeIndex(dataset, dataDirectory);
        logger.info("Build complete");
    }

    private NDCDataset readDataset(Path datasetFile) throws IOException {
        ObjectMapper mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper.readValue(datasetFile.toFile(), NDCDataset.class);
    }

    private void writeIndex(NDCDataset dataset, Path dataDirectory) throws IOException {
        Files.deleteIfExists(dataDirectory);
        Files.createDirectory(dataDirectory);

        Analyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        Directory directory = FSDirectory.open(dataDirectory);
        try (IndexWriter iwriter = new IndexWriter(directory, config)) {
            Iterable<Document> docs = () -> dataset.getResults().stream().map(this::productToDocument).iterator();
            iwriter.addDocuments(docs);
        }
    }

    private Document productToDocument(Product product) {
        try {
            Document doc = new Document();
            doc.add(new Field("productNDC", product.productNDC, TextField.TYPE_STORED));
            if (product.genericName != null) {
                doc.add(new Field("genericName", product.genericName, TextField.TYPE_STORED));
            }
            doc.add(new Field("labelerName", product.labelerName, TextField.TYPE_STORED));
            if (product.brandName != null) {
                doc.add(new Field("brandName", product.brandName, TextField.TYPE_STORED));
            }
            return doc;
        } catch (RuntimeException e) {
            throw new RuntimeException("Error building document for product: " + product, e);
        }
    }
}
