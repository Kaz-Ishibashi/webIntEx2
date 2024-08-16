import java.util.*;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
//import org.jsoup.nodes.Element;

import com.atilika.kuromoji.ipadic.Token;
import com.atilika.kuromoji.ipadic.Tokenizer;

class BoW {
    static Tokenizer tokenizer = new Tokenizer(); // 初期化が遅いので再利用する。
    HashMap<String, Integer> termCount = new HashMap<>();

    // Work1-2b
    static BoW create(String text) {
        // 1. textを形態素解析する
        List<Token> tokens = BoW.tokenizer.tokenize(text);
        // 2. 不用語を除去する
        tokens.removeIf(token -> StopWords.isStopWord(token) || token.getPartOfSpeechLevel1().equals("副詞"));
        // 3. BoW を生成する
        return new BoW(tokens);
    }

    // Work1-2b
    static BoW fetch(String url) {
        try {
            // 実装してみよう
            // 1. urlからHTMLコンテンツの本文を取得する。
            Document doc = Jsoup.connect(url).get();
            // 2. BoWクラスを初期化して bow 変数に格納する。
            BoW bow = BoW.create(doc.body().text());
            // 3. bow を返す
            return bow;
        } catch (Exception e) {
            System.err.println(e);
            return null;
        }
    }

    // Work1-2b
    BoW(List<Token> tokens) {
        for (Token token: tokens) {
            // 1. 語の表現形式を統一する
            String term = BoW.repr(token);

            // 2. termCount 中の語 term をカウントアップ
            this.termCount.put(term, this.termCount.getOrDefault(term, 0) + 1);
        }
    }

    static List<BoW> fetchBySentence(String url) {
        try {
            // 実装してみよう
            // 1. urlからHTMLコンテンツの本文を取得する。
            Document doc = Jsoup.connect(url).get();
            String body = doc.body().text();

            // 2. 文ごとにBoWクラスを初期化して bow 変数に格納する。
            String[] sentences = body.split("。");
            List<BoW> bows = new ArrayList<>();
            for (String sentence : sentences) {
                bows.add(BoW.create(sentence));
            }
            return bows;
        } catch (Exception e) {
            System.err.println(e);
            throw new RuntimeException(e);
        }
    }

    public String toString() {
        return this.termCount.toString();
    }

    static String repr(Token token) {
        return token.getBaseForm();
    }
}

class StopWords {
    static HashSet<String> contentWords = new HashSet<>() {{
        add("名詞");
//        add("動詞");
//        add("形容詞");
//        add("副詞");
    }};

    static boolean isStopWord(Token token) {
        return (StopWords.contentWords.contains(token.getPartOfSpeechLevel1()) == false);
    }
}

class DuosArr {
    private List<Duo> duosArr;

    public DuosArr() {
        this.duosArr = new ArrayList<>();
    }

    public void add(Duo duo) {
        this.duosArr.add(duo);
    }

    public void add(String former, String latter) {
        this.duosArr.add(new Duo(former, latter));
    }

    public List<String> getBuddy(String subj) {
        List<String> buddies = new ArrayList<>();
        for (Duo duo : this.duosArr) {
            if (duo.getFormer().equals(subj)) {
                buddies.add(duo.getLatter());
            } else if (duo.getLatter().equals(subj)) {
                buddies.add(duo.getFormer());
            }
        }
        return buddies;
    }

    public int len() {
        return this.duosArr.size();
    }

    public Duo getDuoByIndex(int i) {
        return this.duosArr.get(i);
    }

    public List<Duo> getDuosArr() {
        return this.duosArr;
    }

    public String getFormerByIndex(int index) {
        if (index < 0 || index >= this.len())
            throw new IndexOutOfBoundsException("I have only " + this.len() + " Duos.");
        else
            return getDuoByIndex(index).getFormer();
    }

    public String getLatterByIndex(int index) {
        if (index < 0 || index >= this.len())
            throw new IndexOutOfBoundsException("I have only " + this.len() + " Duos.");
        else
            return getDuoByIndex(index).getLatter();
    }

    public boolean contains(Duo duo) {
        return this.duosArr.contains(duo);
    }
}

class Duo {
    private String former;
    private String latter;

    Duo(String former, String latter) {
        this.former = former;
        this.latter = latter;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Duo other = (Duo) obj;
        if (this.former.equals(other.former) && this.latter.equals(other.latter)) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.former, this.latter);
    }

    public String getFormer() {
        return this.former;
    }

    public String getLatter() {
        return this.latter;
    }
}
