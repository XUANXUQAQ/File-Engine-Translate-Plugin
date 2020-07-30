package FileEngine.translate.Plugin.translate;

import com.alibaba.fastjson.JSONObject;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class TranslateUtil {
    public static String getTranslation(String str, String fromLang, String toLang) {
        //拼接API网址
        try {
            String address = "http://translate.google.cn/translate_a/single?client=gtx&dt=t&dj=1&ie=UTF-8&sl=" + fromLang + "&tl=" + toLang +
                "&q=" + URLEncoder.encode(str, "UTF-8");
            JSONObject translateJson = getWebInfo(address);
            JSONObject result = translateJson.getJSONArray("sentences").getJSONObject(0);
            return result.getString("trans");
        }catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private static JSONObject getWebInfo(String address) throws IOException {
        URL url = new URL(address);
        StringBuilder strBuilder = new StringBuilder();
        String s;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8))) {
            while ((s = reader.readLine()) != null) {
                strBuilder.append(s);
            }
        }
        return JSONObject.parseObject(strBuilder.toString());
    }
}
