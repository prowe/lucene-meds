package com.sourceallies.lucinemeds;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.simple.SimpleQueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.util.QueryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
@Profile("!index")
public class SearchMedicationCodesController {
    private static final Logger logger = LoggerFactory.getLogger(SearchMedicationCodesController.class);

    private final Set<String> fieldsToLoad = Set.of("_source");
    private final IndexSearcher indexSearcher;
    private final ObjectMapper objectMapper;
    private final SimpleQueryParser queryParser;
    
    public SearchMedicationCodesController(IndexSearcher indexSearcher,
            Analyzer analyzer,
            ObjectMapper objectMapper) {
        this.indexSearcher = indexSearcher;
        this.objectMapper = objectMapper;
        // This configures the weights for different fields
        this.queryParser = new SimpleQueryParser(analyzer, Map.of(
            "productNDC", 1f,
            "genericName", 1f,
            "labelerName", 1f,
            "brandName", 1f
        ));
    }

    @QueryMapping
    public List<NDCProduct> searchMedicationCodes(@Argument("query") String queryText) throws IOException {
        logger.info("Searching for medication codes {}", queryText);
        Query query = queryParser.parse(queryText);
        ScoreDoc[] hits = indexSearcher.search(query, 10).scoreDocs;
        return Arrays.stream(hits)
            .map(this::loadProduct)
            .collect(Collectors.toList());
    }

    private NDCProduct loadProduct(ScoreDoc scoreDoc) {
        try {
            Document doc = indexSearcher.doc(scoreDoc.doc, fieldsToLoad);
            String jsonText = doc.getField("_source").getCharSequenceValue().toString();
            return objectMapper.readValue(jsonText, NDCProduct.class);
        } catch (IOException e) {
            throw new RuntimeException("Unable to load document id: " + scoreDoc.doc, e);
        }
    }
}
