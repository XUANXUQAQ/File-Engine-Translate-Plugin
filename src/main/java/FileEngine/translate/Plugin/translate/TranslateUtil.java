package FileEngine.translate.Plugin.translate;

import FileEngine.translate.Plugin.translate.BaiduAPI.TransApi;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class TranslateUtil {
    private final ConcurrentLinkedQueue<Method> TRANSLATE_API_QUEUE = new ConcurrentLinkedQueue<>();
    private static final TranslateUtil instance = new TranslateUtil();
    private final ConcurrentHashMap<String, String> GOOGLE_LANG_ABBR_2_BAIDU_LANG_ABBR = new ConcurrentHashMap<>();

    private TranslateUtil() {
        try {
            TRANSLATE_API_QUEUE.add(TranslateUtil.class.getDeclaredMethod("getTranslationByGoogleApi",
                    String.class, TranslationString.class, String.class, String.class));
            TRANSLATE_API_QUEUE.add(TranslateUtil.class.getDeclaredMethod("getTranslationByBaiduApi",
                    String.class, TranslationString.class, String.class, String.class));

            //todo 添加语言
            GOOGLE_LANG_ABBR_2_BAIDU_LANG_ABBR.put("zh_CN", "zh");
            GOOGLE_LANG_ABBR_2_BAIDU_LANG_ABBR.put("ja", "jp");
            GOOGLE_LANG_ABBR_2_BAIDU_LANG_ABBR.put("ko", "kor");
            GOOGLE_LANG_ABBR_2_BAIDU_LANG_ABBR.put("fr", "fra");
            GOOGLE_LANG_ABBR_2_BAIDU_LANG_ABBR.put("es", "spa");
            GOOGLE_LANG_ABBR_2_BAIDU_LANG_ABBR.put("vi", "vie");
            GOOGLE_LANG_ABBR_2_BAIDU_LANG_ABBR.put("ar", "ara");
            GOOGLE_LANG_ABBR_2_BAIDU_LANG_ABBR.put("da", "dan");

        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public static TranslateUtil getInstance() {
        return instance;
    }

    public String getTranslation(String str, String fromLang, String toLang) throws IOException, InvocationTargetException, IllegalAccessException {
        TranslationString translationString = new TranslationString();
        getTranslationByBaiduApi(str, translationString, fromLang, toLang);
        if (translationString.isStrTranslated()) {
            return translationString.getTranslation();
        }
        for (Method api : TRANSLATE_API_QUEUE) {
            api.invoke(instance, str, translationString,fromLang, toLang);
            if (translationString.isStrTranslated()) {
                return translationString.getTranslation();
            }
        }
        throw new IOException();
    }

    private void getTranslationByBaiduApi(String str, TranslationString string, String fromLang, String toLang) {
        if (GOOGLE_LANG_ABBR_2_BAIDU_LANG_ABBR.containsKey(fromLang)) {
            fromLang = GOOGLE_LANG_ABBR_2_BAIDU_LANG_ABBR.get(fromLang);
        }
        if (GOOGLE_LANG_ABBR_2_BAIDU_LANG_ABBR.containsKey(toLang)) {
            toLang = GOOGLE_LANG_ABBR_2_BAIDU_LANG_ABBR.get(toLang);
        }
        String trans = new TransApi("20200726000526910", "96r4ohd1IRLvZVhKj9sa").getTransResult(str, fromLang, toLang);
        JSONArray array = JSONObject.parseObject(trans).getJSONArray("trans_result");
        if (array != null) {
            JSONObject json = array.getJSONObject(0);
            trans = json.getString("dst");
            if (!trans.isEmpty()) {
                string.setTranslated();
                string.setTranslation(trans);
            } else {
                string.setTranslation("");
            }
        } else {
            string.setTranslation("");
        }
    }

    private void getTranslationByGoogleApi(String str, TranslationString string, String fromLang, String toLang) {
        try {
            //拼接API网址
            String address = "http://translate.google.cn/translate_a/single?client=gtx&dt=t&dj=1&ie=UTF-8&sl=" + fromLang + "&tl=" + toLang +
                    "&q=" + URLEncoder.encode(str, "UTF-8");
            JSONObject translateJson = getWebInfo(address);
            JSONObject result = translateJson.getJSONArray("sentences").getJSONObject(0);
            string.setTranslated();
            string.setTranslation(result.getString("trans"));
        }catch (IOException e) {
            string.setTranslation("");
        }
    }

    private JSONObject getWebInfo(String address) throws IOException {
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

    private static class TranslationString {
        private boolean isTranslated = false;
        private String translation = "";

        public void setTranslated() {
            this.isTranslated = true;
        }

        public void setTranslation(String str) {
            this.translation = str;
        }

        public boolean isStrTranslated() {
            return isTranslated;
        }

        public String getTranslation() {
            return translation;
        }
    }
}
