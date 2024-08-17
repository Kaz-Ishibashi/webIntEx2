// NLPService.java
// 自然言語処理機能を抽象化するインターフェースです。

import java.util.List;

public interface NLPService {
    List<String> extractNouns(String text);
}