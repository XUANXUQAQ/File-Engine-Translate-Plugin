package file.engine.translate.Plugin.settings;

import file.engine.translate.Plugin.PluginMain;
import file.engine.translate.Plugin.threadPool.CachedThreadPool;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class Settings {
    private static final String configsPath = "plugins/Plugin configuration files/Translate/settings.json";
    private JLabel labelTranslateTip;
    private JLabel labelTranslateFrom;
    private JLabel labelFromLang;
    private JLabel labelToLang;
    private JButton buttonSave;
    private JTextField textFieldSearchFromLang;
    private JList<Object> listFromLang;
    private JTextField textFieldSearchToLang;
    private JList<Object> listToLang;
    private JPanel panel;
    private JLabel labelPlaceHolder;
    private JLabel labelSearchTip;
    private JLabel labelSearchTip2;
    private JScrollPane scrollPaneFromLang;
    private JScrollPane scrollPaneToLang;
    private String fromLangName;
    private String toLangName;
    private volatile String fromLang;
    private volatile String toLang;
    private final JFrame frame = new JFrame("Language Settings");
    private final ConcurrentHashMap<String, String> Name_Abbreviation_map = new ConcurrentHashMap<>();
    private volatile boolean isStartSearchFromLang;
    private volatile boolean isStartSearchToLang;

    public static JSONObject readSettings() {
        StringBuilder strBuilder = new StringBuilder();
        String eachLine;
        try (BufferedReader buffr = new BufferedReader(new InputStreamReader(new FileInputStream(configsPath), StandardCharsets.UTF_8))) {
            while ((eachLine = buffr.readLine()) != null) {
                strBuilder.append(eachLine);
            }
            if (strBuilder.length() == 0) {
                throw new IOException("No content");
            }
            return JSONObject.parseObject(strBuilder.toString());
        } catch (IOException e) {
            initSettings();
            return readSettings();
        }
    }

    private static void initSettings() {
        JSONObject settings = new JSONObject();
        settings.put("fromLang", "ZH_CN");
        settings.put("toLang", "EN");
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void saveSettingsToFile(String fromLang, String toLang) {
        JSONObject json = readSettings();
        json.put("fromLang", fromLang);
        json.put("toLang", toLang);
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(configsPath), StandardCharsets.UTF_8))) {
            String format = JSON.toJSONString(json, SerializerFeature.PrettyFormat, SerializerFeature.WriteMapNullValue, SerializerFeature.WriteDateUseDateFormat);
            bw.write(format);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class SettingsBuilder {
        private static final Settings INSTANCE = new Settings();
    }

    public static Settings getInstance() {
        return SettingsBuilder.INSTANCE;
    }

    public String getFromLang() {
        return fromLang;
    }

    public String getToLang() {
        return toLang;
    }

    public void setFromLangName(String langName) {
        fromLangName = langName;
    }

    public void setToLangName(String langName) {
        toLangName = langName;
    }

    public void setFromLang(String lang) {
        fromLang = lang;
    }

    public void setToLang(String lang) {
        toLang = lang;
    }

    private Settings() {
        addTextFieldSearchListener();
        addButtonSaveListener();
        addSearchThreads();
        addShowSelectedLang();
        initLanguageMap();
    }

    private void addShowSelectedLang() {
        listFromLang.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                String selected = (String) listFromLang.getSelectedValue();
                labelFromLang.setText(selected);
            }
        });
        listToLang.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                String selected = (String) listToLang.getSelectedValue();
                labelToLang.setText(selected);
            }
        });
    }

    public String getAbbreviationByLangName(String value) {
        for (String key : Name_Abbreviation_map.keySet()) {
            if (Name_Abbreviation_map.get(key).equals(value)) {
                return key;
            }
        }
        return "";
    }

    private void initLanguageMap() {
        //todo 添加语言
        Name_Abbreviation_map.put("英语", "en");
        Name_Abbreviation_map.put("中文", "zh_CN");
        Name_Abbreviation_map.put("日语", "ja");
        Name_Abbreviation_map.put("韩语", "ko");
        Name_Abbreviation_map.put("法语", "fr");
        Name_Abbreviation_map.put("俄语", "ru");
        Name_Abbreviation_map.put("西班牙语", "es");
        Name_Abbreviation_map.put("葡萄牙语", "pt");
        Name_Abbreviation_map.put("意大利语", "it");
        Name_Abbreviation_map.put("越南语", "vi");
        Name_Abbreviation_map.put("印度尼西亚语", "id");
        Name_Abbreviation_map.put("阿拉伯语", "ar");
        Name_Abbreviation_map.put("丹麦语", "da");
        Name_Abbreviation_map.put("德语", "de");
        Name_Abbreviation_map.put("希腊语", "el");
        Name_Abbreviation_map.put("波兰语", "pl");
    }

    public void showWindow() {
        listFromLang.setListData(Name_Abbreviation_map.keySet().toArray());
        listToLang.setListData(Name_Abbreviation_map.keySet().toArray());
        listFromLang.setSelectedValue(fromLangName, true);
        listToLang.setSelectedValue(toLangName, true);
        labelFromLang.setText(fromLangName);
        labelToLang.setText(toLangName);
        textFieldSearchFromLang.setText("");
        textFieldSearchToLang.setText("");
        frame.setContentPane(SettingsBuilder.INSTANCE.panel);
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        frame.setSize(600, 800);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void addButtonSaveListener() {
        buttonSave.addActionListener(e -> {
            try {
                fromLangName = (String) listFromLang.getSelectedValue();
                toLangName = (String) listToLang.getSelectedValue();
                fromLang = Name_Abbreviation_map.get(fromLangName);
                toLang = Name_Abbreviation_map.get(toLangName);
                if (fromLang == null) {
                    fromLang = "ZH_CN";
                }
                if (toLang == null) {
                    toLang = "EN";
                }
                saveSettingsToFile(fromLang, toLang);
                frame.setVisible(false);
            } catch (NullPointerException e1) {
                JOptionPane.showMessageDialog(frame, "您未选中任何语言");
            }
        });
    }

    private void addTextFieldSearchListener() {
        textFieldSearchFromLang.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                isStartSearchFromLang = true;
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                isStartSearchFromLang = true;
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
            }
        });

        textFieldSearchToLang.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                isStartSearchToLang = true;
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                isStartSearchToLang = true;
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
            }
        });
    }

    private void addSearchThreads() {
        CachedThreadPool instance = CachedThreadPool.getInstance();
        instance.execute(() -> {
            try {
                HashSet<String> langSet = new HashSet<>();
                while (PluginMain.isNotExit) {
                    if (isStartSearchFromLang) {
                        isStartSearchFromLang = false;
                        langSet.clear();
                        labelFromLang.setText("");
                        String text = textFieldSearchFromLang.getText().toLowerCase();
                        if (text.isEmpty()) {
                            listFromLang.setListData(Name_Abbreviation_map.keySet().toArray());
                        } else {
                            for (String each : Name_Abbreviation_map.keySet()) {
                                if (each.toLowerCase().contains(text)) {
                                    langSet.add(each);
                                }
                            }
                            listFromLang.setListData(langSet.toArray());
                        }
                    }
                    TimeUnit.MILLISECONDS.sleep(50);
                }
            } catch (InterruptedException ignored) {
            }
        });

        instance.execute(() -> {
            try {
                HashSet<String> langSet = new HashSet<>();
                while (PluginMain.isNotExit) {
                    if (isStartSearchToLang) {
                        langSet.clear();
                        isStartSearchToLang = false;
                        labelToLang.setText("");
                        String text = textFieldSearchToLang.getText().toLowerCase();
                        if (text.isEmpty()) {
                            listToLang.setListData(Name_Abbreviation_map.keySet().toArray());
                        } else {
                            for (String each : Name_Abbreviation_map.keySet()) {
                                if (each.toLowerCase().contains(text)) {
                                    langSet.add(each);
                                }
                            }
                            listToLang.setListData(langSet.toArray());
                        }
                    }
                    TimeUnit.MILLISECONDS.sleep(50);
                }
            } catch (InterruptedException ignored) {
            }
        });
    }
}
