package FileEngine.translate.Plugin.settings;

import FileEngine.translate.Plugin.PluginMain;
import FileEngine.translate.Plugin.config.ConfigurationUtil;
import FileEngine.translate.Plugin.threadPool.CachedThreadPool;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class Settings {
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
    private String fromLangName;
    private String toLangName;
    private volatile String fromLang;
    private volatile String toLang;
    private final JFrame frame = new JFrame("Language Settings");
    private final ConcurrentHashMap<String, String> Name_Abbreviation_map = new ConcurrentHashMap<>();
    private volatile boolean isStartSearchFromLang;
    private volatile boolean isStartSearchToLang;
    private volatile boolean isInitialized = false;

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
        fromLang =lang;
    }

    public void setToLang(String lang) {
        toLang = lang;
    }

    private Settings() {
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
                ConfigurationUtil.saveSettingsToFile(fromLang, toLang);
                frame.setVisible(false);
            }catch (NullPointerException e1) {
                JOptionPane.showMessageDialog(frame, "您未选中任何语言");
            }
        });
        addTextFieldSearchListener();
        addSearchThreads();
        addShowSelectedLang();
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

    public String getKeyByValue(String value) {
        for(String key: Name_Abbreviation_map.keySet()){
            if(Name_Abbreviation_map.get(key).equals(value)){
               return key;
            }
        }
        return "";
    }

    public void initLanguageMap() {
        if (!isInitialized) {
            isInitialized = true;
            Name_Abbreviation_map.put("English", "en");
            Name_Abbreviation_map.put("Chinese", "zh_CN");
            Name_Abbreviation_map.put("Japanese", "ja");
            Name_Abbreviation_map.put("Korean", "ko");
            Name_Abbreviation_map.put("French", "fr");
            Name_Abbreviation_map.put("Russian", "ru");
            Name_Abbreviation_map.put("Spanish", "es");
            Name_Abbreviation_map.put("Portuguese", "pt");
            Name_Abbreviation_map.put("Italian", "it");
            Name_Abbreviation_map.put("Vietnamese", "vi");
            Name_Abbreviation_map.put("Indonesian", "id");
            Name_Abbreviation_map.put("Arabic", "ar");
        }
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
                    Thread.sleep(50);
                }
            } catch (InterruptedException ignored) {
            }
        });
    }
}
