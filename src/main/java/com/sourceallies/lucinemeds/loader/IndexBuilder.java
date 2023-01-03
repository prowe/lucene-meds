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
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sourceallies.lucinemeds.NDCProduct;

@Component
@Profile("index")
public class IndexBuilder implements ApplicationRunner {
    private static Logger logger = LoggerFactory.getLogger(IndexBuilder.class);

    private final ObjectMapper objectMapper;
    private final Directory dataDirectory;
    private final Analyzer analyzer;

    public IndexBuilder(ObjectMapper objectMapper, Directory dataDirectory, Analyzer analyzer) {
        this.objectMapper = objectMapper;
        this.dataDirectory = dataDirectory;
        this.analyzer = analyzer;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Path datasetFile = Path.of("drug-ndc-0001-of-0001.json");
        logger.info("Building Lucene index using dataset: {}", datasetFile.toAbsolutePath());
        NDCDataset dataset = readDataset(datasetFile);
        writeIndex(dataset);
        logger.info("Build complete");
    }

    private NDCDataset readDataset(Path datasetFile) throws IOException {
        return objectMapper.readValue(datasetFile.toFile(), NDCDataset.class);
    }

    private void writeIndex(NDCDataset dataset) throws IOException {
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        try (IndexWriter iwriter = new IndexWriter(dataDirectory, config)) {
            Iterable<Document> docs = () -> dataset.getResults().stream().map(this::productToDocument).iterator();
            iwriter.addDocuments(docs);
        }
    }

    private Document productToDocument(NDCProduct product) {
        try {
            Document doc = new Document();
            doc.add(new Field("productNDC", product.getProductNDC(), TextField.TYPE_NOT_STORED));
            if (product.getGenericName() != null) {
                doc.add(new Field("genericName", product.getGenericName(), TextField.TYPE_NOT_STORED));
            }
            doc.add(new Field("labelerName", product.getLabelerName(), TextField.TYPE_NOT_STORED));
            if (product.getBrandName() != null) {
                doc.add(new Field("brandName", product.getBrandName(), TextField.TYPE_NOT_STORED));
            }
            doc.add(new Field("_source", objectMapper.writeValueAsString(product), TextField.TYPE_STORED));
            return doc;
        } catch (RuntimeException | JsonProcessingException e) {
            throw new RuntimeException("Error building document for product: " + product, e);
        }
    }
}
