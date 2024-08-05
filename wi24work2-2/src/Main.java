import java.io.*;
import java.util.*;

import javax.net.ssl.HttpsURLConnection;
import java.net.*;
import org.jsoup.*;

import com.atilika.kuromoji.ipadic.Tokenizer;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;


public class Main {

	// Google Knowledge Graph Search APIの認証用API Keyに変える 
	static String gkgsApiKey = "xxxxxxx"; 
	
	public static void main(String[] args) throws IOException {
		
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		while (true) {
			System.out.print("You > ");
			String userInput = in.readLine();  // 標準入力からのユーザ発話を取得
			if (userInput.equals("ばいばい") || userInput.equals("exit")) {
				System.out.println("Bot > ばいばい");
				break;
			} else {
				String botResp = generateResponse(userInput);  // botの返答を生成
				System.out.println("Bot > " + botResp);
			}
		}
		System.exit(0);	
		
		/* Work 2-1 関係のソースコードをコメントアウトしました
		System.out.println("Wikidata上の名古屋工業大学");
		String wdJson = getWikidataJson("名古屋工業大学");
		System.out.println("[JSON]\n" + wdJson);
		Map<String, Object> wdMap = json2Map(wdJson);
		System.out.println("[Map]\n" + wdMap);
				
		System.out.println("\n----------\n");

		// getPropValsのテスト
		System.out.println("[getPropValsのテスト]");
		String prop = "P856";
		List<Map<String, Object>> resList = (List<Map<String, Object>>) wdMap.get("result");
		for (Map<String, Object> res: resList) {
			String entityID = getEntityID(res);
			List<String> propVals = getPropVals(res, prop);
			System.out.println("エンティティ "+entityID+"のプロパティ"+prop+": " + propVals);
		}
		*/
	}

	/**
	 * botの返答を生成して返す
	 * @param userInput
	 * @return 生成したbotの返答
	 */
	public static String generateResponse(String userInput) {
		return "「"+userInput+"」ですか、へぇー";
	}
	
	/**
	 * 
	 * @param res
	 * @return
	 */
	public static String getEntityID(Map<String, Object> res) {
		return (String)((Map)res.get("entities")).keySet().iterator().next();
	}
	
	/**
	 * 
	 * @param map
	 * @param prop
	 * @return
	 */
	public static List<String> getPropVals(Map<String, Object> res, String prop) {
		List<String> vals = new ArrayList<String>();
		String entityID = getEntityID(res);
		Map entityMap = (Map)((Map)res.get("entities")).get(entityID);
		Map claimMap = (Map)entityMap.get("claims");
		if (claimMap != null) {	
			List<Map> propList = (List<Map>)claimMap.get(prop);
			if (propList != null) {
				for (Map propMap: propList) {
					Map<String,Object> valMap = (Map)((Map)propMap.get("mainsnak")).get("datavalue");
					String val = (String)valMap.get("value");
					vals.add(val);
				}
			}
		}	
		return vals;
	}
	
	
	/**
	 * Wikidataからデータを検索
	 * @param query
	 * @return　Wikidataから取得したJSON文字列
	 */
	public static String getWikidataJson(String query) {
		StringBuffer sb = new StringBuffer();
		sb.append("{\"result\":[");
		int initLen = sb.length();
		List<String> ids = getWikidataIds(query);
		for (String id: ids) {
			if (sb.length() > initLen) {
				sb.append(",");
			}
			String url = "https://www.wikidata.org/wiki/Special:EntityData/" + id + ".json";
			String json = getData(url);
			sb.append(getData(url));
		}
		sb.append("]}");
		return sb.toString();
	}
	
	/**
	 * WikidataエンティティのIDを検索
	 * @param query
	 * @return WikidataエンティティのIDのリスト
	 */
	public static List<String> getWikidataIds(String query) {
		String encodedQuery = "";
		try {
			encodedQuery = URLEncoder.encode(query, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}	
		String url = "https://www.wikidata.org/w/api.php?action=wbsearchentities&language=ja&format=json"
		              + "&search=" + encodedQuery;
		Map<String, Object> map = json2Map(getData(url));
		List<Map<String, Object>> list = (List<Map<String, Object>>)map.get("search");
		List<String> ids = new ArrayList<String>();
		for (Map<String, Object> entMap: list) {
			String id = (String)entMap.get("id");
			ids.add(id);
		}
		return ids;
	}
	
	/**
	 * Google Knowledge Graph からデータを検索
	 * @param query
	 * @return Google Knowledge Graphから取得したJSON文字列
	 */
	public static String getGoogleKGJson(String query) {
		String encodedQuery = "";
		try {
			encodedQuery = URLEncoder.encode(query, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}	
		String url = "https://kgsearch.googleapis.com/v1/entities:search?query=" + encodedQuery  
				   + "&languages=ja&key=" + gkgsApiKey;
		return getData(url);
	}
	
	/**
	 * JSON形式の文字列をMapに変換
	 * @param json
	 * @return JSONから変換したMapオブジェクト
	 */
	public static Map<String, Object> json2Map(String json) {
		ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> map = new HashMap<String, Object>();
		map = null;
		try {
			map = mapper.readValue(json, new TypeReference<Map<String, Object>>(){});
		} catch (Exception e) {
			e.printStackTrace();
		}
		return map;
	}
	
	
	/**
	 * HTMLに限らない形式のデータをWebから取得
	 * @param url
	 * @return 返ってきたデータ
	 */
    public static String getData(String url) {
    	String enc = "UTF-8";
    	StringBuffer sb = new StringBuffer();
    	try {
    		BufferedReader in = null;
    		if (url.startsWith("https")) {
    			HttpsURLConnection conn = (HttpsURLConnection)new URL(url).openConnection();
    			in = new BufferedReader(new InputStreamReader(conn.getInputStream(), enc));
    		} else {
    			URLConnection conn = new URL(url).openConnection();
    			in = new BufferedReader(new InputStreamReader(conn.getInputStream(), enc));
    		}
    		for (String line = in.readLine(); line != null; line = in.readLine()) {
    			sb.append(line);
    			sb.append("\n");
    		}
    	} catch (IOException e) {
    		e.printStackTrace();
    	}
    	return sb.toString();
    }


}
