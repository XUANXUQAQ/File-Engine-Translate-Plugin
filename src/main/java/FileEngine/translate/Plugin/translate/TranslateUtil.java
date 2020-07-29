package FileEngine.translate.Plugin.translate;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class TranslateUtil {
    public static String getTranslation(String str, String fromLang, String toLang) {
        //拼接API网址
        String address = "http://fanyi.youdao.com/translate?&doctype=json&type=" + fromLang + "2" + toLang + "&i=" + str;
        JSONObject translateJson = getWebInfo(address);
        if (translateJson != null) {
            JSONObject result = translateJson.getJSONArray("translateResult").getJSONArray(0).getJSONObject(0);
            return result.getString("tgt");
        } else {
            return "";
        }
    }

    private static JSONObject getWebInfo(String address) {
        BufferedReader reader = null;
        StringBuilder strBuilder = new StringBuilder();
        try {
            URL url = new URL(address);
            reader = new BufferedReader(new InputStreamReader(url.openStream()));
            String s;
            while ((s = reader.readLine()) != null) {
                strBuilder.append(s);
            }
        }catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            }catch (Exception ignored) {
            }
        }
        try {
            return JSONObject.parseObject(strBuilder.toString());
        }catch (JSONException e) {
            return null;
        }
    }
}
