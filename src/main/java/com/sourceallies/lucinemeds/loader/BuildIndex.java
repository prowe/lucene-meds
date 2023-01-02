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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class BuildIndex {
    public static void main(String[] args) throws IOException{
        Path datasetFile = Path.of("drug-ndc-0001-of-0001.json");
        Path dataDirectory = Path.of("data");
        System.out.println("Building Lucene index using dataset: " + datasetFile.toAbsolutePath());
        NDCDataset dataset = readDataset(datasetFile);
        writeIndex(dataset, dataDirectory);
        System.out.println("Build complete");
    }

    private static NDCDataset readDataset(Path datasetFile) throws IOException {
        ObjectMapper mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper.readValue(datasetFile.toFile(), NDCDataset.class);
    }

    private static void writeIndex(NDCDataset dataset, Path dataDirectory) throws IOException {
        Files.deleteIfExists(dataDirectory);
        Files.createDirectory(dataDirectory);

        Analyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        Directory directory = FSDirectory.open(dataDirectory);
        try (IndexWriter iwriter = new IndexWriter(directory, config)) {
            Iterable<Document> docs = () -> dataset.getResults().stream().map(BuildIndex::productToDocument).iterator();
            iwriter.addDocuments(docs);
        }
    }

    private static Document productToDocument(Product product) {
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
