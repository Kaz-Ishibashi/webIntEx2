// Main.java
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class Main {
    private static WikidataService wikidataService;
    private static NLPService nlpService;

    public static void main(String[] args) throws IOException {
        // 設定の読み込みと初期化
        Properties config = ConfigLoader.loadConfig("config.properties");
        wikidataService = new WikidataServiceImpl(config.getProperty("wikidata.api.url"));
        nlpService = new NLPServiceImpl();

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            System.out.print("You > ");
            String userInput = reader.readLine();

            if (userInput.equals("exit") || userInput.equals("quit") || userInput.equals("ばいばい")) {
                System.out.println("Bot > ばいばい");
                break;
            }

            String response = generateResponse(userInput);
            System.out.println("Bot > " + response);
        }
    }

    private static String generateResponse(String userInput) {
        // 「AのBは？」形式の質問を処理
        String[] parts = userInput.split("の");
        if (parts.length == 2 && parts[1].endsWith("は？")) {
            String entityA = parts[0].trim();
            String propertyB = parts[1].substring(0, parts[1].length() - 2).trim();

            try {
                // エンティティAのIDを取得
                List<String> entityIds = wikidataService.getWikidataIds(entityA);
                if (entityIds.isEmpty()) {
                    return entityA + "に関する情報が見つかりませんでした。";
                }
                String entityId = entityIds.get(0);

                // プロパティBのIDを取得
                List<String> propIds = wikidataService.getWikidataPropIds(propertyB);
                if (propIds.isEmpty()) {
                    return propertyB + "に関する情報が見つかりませんでした。";
                }
                String propId = propIds.get(0);

                // プロパティ値を取得
                String jsonResponse = wikidataService.getEntityProperty(entityId, propId);
                // JSONレスポンスを解析
                Map<String, Object> responseMap = wikidataService.json2Map(jsonResponse);
                List<String> propValues = WikidataService.getPropVals(responseMap, propId);

                if (propValues.isEmpty()) {
                    return entityA + "の" + propertyB + "に関する情報が見つかりませんでした。";
                }

                // エンティティAとプロパティBのラベルを取得
                String entityALabel = wikidataService.getLabelById(entityId);
                String propertyBLabel = wikidataService.getLabelById(propId);

                // プロパティ値のラベルを取得
                List<String> propValueLabels = propValues.stream()
                        .map(wikidataService::getLabelById)
                        .collect(Collectors.toList());

                // 結果を整形
                String result = String.format("%sの%sは%sです。",
                        entityALabel,
                        propertyBLabel,
                        String.join("、", propValueLabels));

                return result;

            } catch (Exception e) {
                return "申し訳ありません。情報の取得中にエラーが発生しました: " + e.getMessage();
            }
        }

        // その他の入力の場合はデフォルトの応答
        return "申し訳ありません。「AのBは？」の形式で質問してください。";
    }
}