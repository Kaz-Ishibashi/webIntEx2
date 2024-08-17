// NLPServiceImpl.java
// NLPServiceインターフェースの実装クラスです。

import com.atilika.kuromoji.ipadic.Token;
import com.atilika.kuromoji.ipadic.Tokenizer;
import java.util.List;
import java.util.stream.Collectors;

public class NLPServiceImpl implements NLPService {
    private final Tokenizer tokenizer;

    public NLPServiceImpl() {
        this.tokenizer = new Tokenizer();
    }

    @Override
    public List<String> extractNouns(String text) {
        return tokenizer.tokenize(text).stream()
                .filter(token -> token.getPartOfSpeechLevel1().equals("名詞"))
                .map(Token::getSurface)
                .collect(Collectors.toList());
    }
}