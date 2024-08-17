// WikidataService.java
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public interface WikidataService {
    String getWikidataJson(String query);
    List<String> getWikidataIds(String query);
    List<String> getWikidataPropIds(String query);
    Map<String, Object> json2Map(String json);
    String getEntityID(Map<String, Object> res);
    String getLabelById(String id);
    public static List<String> getPropVals(Map<String, Object> res, String prop) {
        List<String> propVals = new ArrayList<String>();
        Map<String, Object> entity = (Map<String, Object>) res.get("entities");
        for (Map.Entry<String, Object> entry: entity.entrySet()) {
            Map<String, Object> claims = (Map<String, Object>) entry.getValue();
            List<Map<String, Object>> propList = (List<Map<String, Object>>) claims.get(prop);
            if (propList != null) {
                for (Map<String, Object> propMap: propList) {
                    Map<String,Object> valMap = (Map) ((Map) propMap.get("mainsnak")).get("datavalue");
                    Object val = propMap.get("value");
                    if (val instanceof String) {
                        propVals.add((String) val);
                    }
                    Map<String, Object> mainsnak = (Map<String, Object>) propMap.get("mainsnak");
                    Map<String, Object> datavalue = (Map<String, Object>) mainsnak.get("datavalue");
                    Map<String, Object> value = (Map<String, Object>) datavalue.get("value");
                    propVals.add((String) value.get("id"));
                }
            }
        }
        return propVals;
    }

    // 新しいメソッド：エンティティのプロパティ値を取得
    String getEntityProperty(String entityId, String propertyId);
}