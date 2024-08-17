// WikidataServiceImpl.java
// WikidataServiceインターフェースの実装クラスです。

import com.fasterxml.jackson.core.type.TypeReference;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class WikidataServiceImpl implements WikidataService {
    private final String apiUrl;
    private final ObjectMapper objectMapper;

    public WikidataServiceImpl(String apiUrl) {
        this.apiUrl = apiUrl;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String getWikidataJson(String query) {
        List<String> ids = getWikidataIds(query);
        return ids.stream()
                .map(id -> "https://www.wikidata.org/wiki/Special:EntityData/" + id + ".json")
                .map(this::getData)
                .collect(Collectors.joining(",", "{\"result\":[", "]}"));
    }

    @Override
    public String getLabelById(String id) {
        String url = apiUrl + "?action=wbgetentities&ids=" + id + "&props=labels&languages=ja&format=json";
        try {
            String jsonResponse = getData(url);
            Map<String, Object> responseMap = json2Map(jsonResponse);
            Map<String, Object> entities = (Map<String, Object>) responseMap.get("entities");
            Map<String, Object> entity = (Map<String, Object>) entities.get(id);
            Map<String, Object> labels = (Map<String, Object>) entity.get("labels");
            Map<String, Object> jaLabel = (Map<String, Object>) labels.get("ja");
            return (String) jaLabel.get("value");
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch label for ID: " + id, e);
        }
    }

    @Override
    public List<String> getWikidataIds(String query) {
        String url = apiUrl + "?action=wbsearchentities&language=ja&format=json&search="
                + URLEncoder.encode(query, StandardCharsets.UTF_8);
        String jsonResponse = getData(url);
        Map<String, Object> responseMap = json2Map(jsonResponse);
        List<Map<String, Object>> searchResults = (List<Map<String, Object>>) responseMap.get("search");
        return searchResults.stream()
                .map(result -> (String) result.get("id"))
                .collect(Collectors.toList());
    }

    public List<String> getWikidataPropIds(String query) {
        String url = apiUrl + "?action=wbsearchentities&language=ja&format=json&type=property&search="
                + URLEncoder.encode(query, StandardCharsets.UTF_8);
        String jsonResponse = getData(url);
        Map<String, Object> responseMap = json2Map(jsonResponse);
        List<Map<String, Object>> searchResults = (List<Map<String, Object>>) responseMap.get("search");
        return searchResults.stream()
                .map(result -> (String) result.get("id"))
                .collect(Collectors.toList());
    }

    @Override
    public String getEntityProperty(String entityId, String propertyId) {
        String url = apiUrl + "?action=wbgetclaims&entity=" + entityId + "&property=" + propertyId + "&format=json";
        try {
            Document doc = Jsoup.connect(url).ignoreContentType(true).get();
            return doc.body().text();
        } catch (IOException e) {
            throw new RuntimeException("Failed to fetch data from URL: " + url, e);
        }
    }

    @Override
    public Map<String, Object> json2Map(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (IOException e) {
            throw new RuntimeException("JSON parsing failed", e);
        }
    }

    @Override
    public String getEntityID(Map<String, Object> res) {
        return ((Map<String, Object>) res.get("entities")).keySet().iterator().next();
    }



    private String getData(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
            }

            Scanner scanner = new Scanner(conn.getInputStream(), StandardCharsets.UTF_8.name());
            String response = scanner.useDelimiter("\\A").next();
            scanner.close();
            conn.disconnect();
            return response;
        } catch (IOException e) {
            throw new RuntimeException("Failed to fetch data from URL: " + urlString, e);
        }
    }
}