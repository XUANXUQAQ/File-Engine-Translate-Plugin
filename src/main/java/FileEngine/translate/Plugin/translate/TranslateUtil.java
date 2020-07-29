package FileEngine.translate.Plugin.translate;

import com.alibaba.fastjson.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class TranslateUtil {
    public static String getTranslation(String str, String fromLang, String toLang) {
        //拼接API网址
        String address = "http://translate.google.cn/translate_a/single?client=gtx&dt=t&dj=1&ie=UTF-8&sl=" + fromLang + "&tl=" + toLang + "&q=" + str;
        try {
            Thread.sleep(1500);
            JSONObject translateJson = getWebInfo(address);
            JSONObject result = translateJson.getJSONArray("sentences").getJSONObject(0);
            return result.getString("trans");
        }catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private static JSONObject getWebInfo(String address) throws IOException, URISyntaxException {
        StringBuilder strBuilder = new StringBuilder();
        URL url = new URL(address);
        URI uri  = new URI(url.getProtocol(), url.getHost(), url.getPath(), url.getQuery(), null);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(uri.toURL().openStream()))) {
            String s;
            while ((s = reader.readLine()) != null) {
                strBuilder.append(s);
            }
        }
        return JSONObject.parseObject(strBuilder.toString());
    }
}
