// ResponseGenerator.java
// ユーザーの入力に基づいて応答を生成するクラスです。

import java.util.*;
import java.util.stream.Collectors;

public class ResponseGenerator {
    private static final String DEFAULT_PROPERTY = "P279";
    private static final String FALLBACK_RESPONSE = "へぇー。%sについては特に情報が見つかりませんでした。";

    private final WikidataService wikidataService;
    private final NLPService nlpService;
    private final Random random;

    public ResponseGenerator(WikidataService wikidataService, NLPService nlpService) {
        this.wikidataService = wikidataService;
        this.nlpService = nlpService;
        this.random = new Random();
    }

    public String generateResponse(String userInput) {
        List<String> nouns = nlpService.extractNouns(userInput);
        if (nouns.isEmpty()) {
            return String.format("「%s」ですか、へぇー", userInput);
        }

        String selectedNoun = nouns.get(random.nextInt(nouns.size()));
        String wikidataJson = wikidataService.getWikidataJson(selectedNoun);
        Map<String, Object> wikidataMap = wikidataService.json2Map(wikidataJson);

        List<Map<String, Object>> resultList = (List<Map<String, Object>>) wikidataMap.get("result");
        List<String> propertyValues = resultList.stream()
                .flatMap(res -> WikidataService.getPropVals(res, DEFAULT_PROPERTY).stream())
                .collect(Collectors.toList());

        if (propertyValues.isEmpty()) {
            return String.format(FALLBACK_RESPONSE, selectedNoun);
        }

        String randomValue = propertyValues.get(random.nextInt(propertyValues.size()));
        return String.format("へぇー。%sといえば、WikiDataでは%sのサブクラスですよ", selectedNoun, randomValue);
    }
}