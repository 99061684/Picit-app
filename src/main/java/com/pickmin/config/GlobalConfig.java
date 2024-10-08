package com.pickmin.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.pickmin.translation.Language;
import com.pickmin.translation.TranslationHelper;

public class GlobalConfig {
    private static final String SECRET_CONFIGURATION_FILE = "secret.properties";
    private static final Properties properties;

    // Bestandslocaties
    public static final String DATA_FILE_PATH;
    public static final String USERS_FILE_PATH;
    public static final String PRODUCTS_FILE_PATH;

    // functionaliteiten
    public static final Boolean SAVE_PRODUCTS_AFTER_CREATE = true;
    public static final Boolean SAVE_PRODUCTS_AFTER_CHANGE = true;
    public static final Boolean SAVE_PRODUCTS_AFTER_DELETE = false;
    public static final Boolean SAVE_SHOPPINGLIST = false;
    public static final Boolean LOAD_SHOPPINGLIST = false;

    // console output
    public static final Boolean EMPTY_FILE_ERROR_CONSOLE = true;
    public static final Boolean EMPTY_FILE_ERROR_PATH_CONSOLE = true;
    public static final Boolean NO_TRANSLATION_KEY_ERROR_CONSOLE = true;

    // Schermconfiguratie
    public static final int DEFAULT_WIDTH = 800;
    public static final int DEFAULT_HEIGHT = 600;

    // Validation
    public static final int BCRYPT_STRENGTH = 10; // sterkte van de BCryptPasswordEncoder (voor wachtwoorden)
    public static final Boolean PASSWORD_CREATE_PATTERN = true;

    // Standaard taal van de applicatie
    public static final Language DEFAULT_LANGUAGE = Language.DUTCH;

    // Huidige taal (kan tijdens runtime veranderen)
    public static Language currentLanguage = DEFAULT_LANGUAGE;

    // Methode om de huidige taal te wijzigen
    public static void setLanguage(Language language) {
        currentLanguage = language;
        TranslationHelper.setLanguage(language);
    }

    static {
        properties = new Properties();
        loadSecretConfigurationFile();
        DATA_FILE_PATH = getConfigurationValue("GlobalConfig.DATA_FILE_PATH");
        USERS_FILE_PATH = DATA_FILE_PATH + "users.json";
        PRODUCTS_FILE_PATH = DATA_FILE_PATH + "products.json";
    }

    // Methode om het pad van het secret bestand te laden
    public static void loadSecretConfigurationFile() {
        try (InputStream inputStream = GlobalConfig.class.getResourceAsStream(SECRET_CONFIGURATION_FILE)) {
            properties.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file " + SECRET_CONFIGURATION_FILE, e);
        }
    }

    public static HashMap<String, Object> getConfiguration() {
        // ugly workaround to get String as generics
        Map temp = properties;
        HashMap<String, Object> map = new HashMap<String, Object>(temp);
        return map;
    }

    public static String getConfigurationValue(String key) {
        return properties.getProperty(key);
    }
}