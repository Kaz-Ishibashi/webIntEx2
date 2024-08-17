// ConfigLoader.java
// 設定ファイルを読み込むユーティリティクラスです。

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigLoader {
    public static Properties loadConfig(String filename) {
        Properties prop = new Properties();
        try (InputStream input = ConfigLoader.class.getClassLoader().getResourceAsStream(filename)) {
            if (input == null) {
                throw new RuntimeException("Unable to find " + filename);
            }
            prop.load(input);
        } catch (IOException ex) {
            throw new RuntimeException("Failed to load config file", ex);
        }
        return prop;
    }
}