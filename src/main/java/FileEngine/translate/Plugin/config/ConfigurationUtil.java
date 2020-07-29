package FileEngine.translate.Plugin.config;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class ConfigurationUtil {
    private static final String configsPath = "plugins/Plugin configuration files/Translate/settings.json";

    public static JSONObject readSettings() {
        StringBuilder strBuilder =  new StringBuilder();
        String eachLine;
        try (BufferedReader buffr = new BufferedReader(new InputStreamReader(new FileInputStream(configsPath), StandardCharsets.UTF_8))) {
            while ((eachLine = buffr.readLine()) != null) {
                strBuilder.append(eachLine);
            }
            if (strBuilder.length() == 0) {
                throw new IOException("No content");
            }
            return JSONObject.parseObject(strBuilder.toString());
        }catch (IOException e) {
            initSettings();
            return readSettings();
        }
    }

    public static void initSettings() {
        JSONObject settings = new JSONObject();
        settings.put("fromLang", "ZH_CN");
        settings.put("toLang", "EN");
        settings.put("labelColor", 0xcccccc);
        settings.put("backgroundColor", 0xffffff);
        File configFile = new File(configsPath);
        File parent = configFile.getParentFile();
        if (!parent.exists()) {
            parent.mkdirs();
        }
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
            } catch (IOException ignored) {
            }
        }
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(configsPath), StandardCharsets.UTF_8))) {
            String format = JSON.toJSONString(settings, SerializerFeature.PrettyFormat, SerializerFeature.WriteMapNullValue, SerializerFeature.WriteDateUseDateFormat);
            bw.write(format);
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveSettingsToFile(String fromLang, String toLang) {
        JSONObject json = readSettings();
        json.put("fromLang", fromLang);
        json.put("toLang", toLang);
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(configsPath), StandardCharsets.UTF_8))) {
            String format = JSON.toJSONString(json, SerializerFeature.PrettyFormat, SerializerFeature.WriteMapNullValue, SerializerFeature.WriteDateUseDateFormat);
            bw.write(format);
        }catch (IOException e) {
            e.printStackTrace();
        }
    }
}
