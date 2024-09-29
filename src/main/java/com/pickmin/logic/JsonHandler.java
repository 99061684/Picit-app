package com.pickmin.logic;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.pickmin.config.GlobalConfig;
import com.pickmin.exceptions.EmptyFileException;
import com.pickmin.translation.Language;

public class JsonHandler {

    public static ArrayList<Product> loadProductsFromJson() {
        ArrayList<Product> products = new ArrayList<>();
        JSONArray dataArray = getDataArray(GlobalConfig.PRODUCTS_FILE_PATH);

        for (int i = 0; i < dataArray.length(); i++) {
            JSONObject obj = dataArray.getJSONObject(i);
            products.add(getProductFromJson(obj));
        }

        return products;
    }

    public static void saveProductsToJson(ArrayList<Product> products) {
        JSONArray productArray = new JSONArray();

        for (Product product : products) {
            productArray.put(getProductJson(product));
        }

        writeToFile(GlobalConfig.PRODUCTS_FILE_PATH, productArray);
    }

    public static void saveUsersToJson(List<User> users) {
        JSONArray userArray = new JSONArray();
        for (User user : users) {
            userArray.put(getUserJson(user));
        }

        writeToFile(GlobalConfig.USERS_FILE_PATH, userArray);
    }

    public static ArrayList<User> loadUsersFromJson() {
        ArrayList<User> users = new ArrayList<>();
        JSONArray dataArray = getDataArray(GlobalConfig.USERS_FILE_PATH);

        for (int i = 0; i < dataArray.length(); i++) {
            JSONObject obj = dataArray.getJSONObject(i);
            users.add(getUserFromJson(obj));
        }

        return users;
    }

    public static void saveShoppingListToJson(String userId, List<ShoppingListProduct> products) {
        JSONArray jsonArray = new JSONArray();
        for (ShoppingListProduct product : products) {
            jsonArray.put(getShoppingListProductJson(product));
        }

        writeToFile(GlobalConfig.DATA_FILE_PATH + "shoppinglist_" + userId + ".json", jsonArray);
    }

    public static ShoppingList loadShoppingListFromJson(String userId) {
        ArrayList<ShoppingListProduct> products = new ArrayList<>();
        JSONArray dataArray = getDataArray(GlobalConfig.DATA_FILE_PATH + "shoppinglist_" + userId + ".json");

        for (int i = 0; i < dataArray.length(); i++) {
            JSONObject obj = dataArray.getJSONObject(i);
            products.add(getShoppingListProductFromJson(obj));
        }

        ShoppingList shoppingList = new ShoppingList(products);
        return shoppingList;
    }

    // Opslaan van branches
    public static void saveBranchesToJson(List<Branch> branches) {
        JSONArray branchArray = new JSONArray();
        for (Branch branch : branches) {
            branchArray.put(createBranchJSONObject(branch));
        }
        writeToFile("branches.json", branchArray);
    }

    // Laden van branches
    public static List<Branch> loadBranchesFromJson() {
        JSONArray branchArray = getDataArray("branches.json");
        List<Branch> branches = new ArrayList<>();
        for (int i = 0; i < branchArray.length(); i++) {
            JSONObject branchObject = branchArray.getJSONObject(i);
            branches.add(parseBranchJSONObject(branchObject));
        }
        return branches;
    }

    // Opslaan van BranchProduct voor een specifieke branch
    public static void saveBranchProductsToJson(String branchId, List<BranchProduct> branchProducts) {
        JSONArray productArray = new JSONArray();
        for (BranchProduct product : branchProducts) {
            productArray.put(createBranchProductJSONObject(product));
        }
        writeToFile("branch_" + branchId + "_products.json", productArray);
    }

    // Laden van BranchProduct voor een specifieke branch
    public static List<BranchProduct> loadBranchProductsFromJson(String branchId) {
        JSONArray productArray = getDataArray("branch_" + branchId + "_products.json");
        List<BranchProduct> products = new ArrayList<>();
        for (int i = 0; i < productArray.length(); i++) {
            JSONObject productObject = productArray.getJSONObject(i);
            products.add(parseBranchProductJSONObject(productObject));
        }
        return products;
    }

    // ----------------- helper functions -----------------

    // schrijf een JSONArray met data naar een bestand.
    private static void writeToFile(String filePath, JSONArray jsonArray) {
        try (FileWriter file = new FileWriter(filePath)) {
            file.write(jsonArray.toString(4)); // Schrijf netjes de JSON weg met inspringing
            file.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Haal de JSONArray met data uit een bestand op
    private static JSONArray getDataArray(String filePath) {
        JSONArray jsonArray = new JSONArray();

        try {
            // Controleer of het bestand leeg is
            File file = new File(filePath);
            if (file.length() == 0) {
                if (GlobalConfig.EMPTY_FILE_ERROR_PATH_CONSOLE) {
                    throw new EmptyFileException(filePath);
                } else {
                    throw new EmptyFileException();
                }
            }

            // Lees het bestand in als het niet leeg is
            try (FileReader reader = new FileReader(file)) {
                Object json = new JSONTokener(reader).nextValue();
                jsonArray = (JSONArray) json;
            }
        } catch (IOException e) {
            System.out.println("Geen bestand gevonden.");
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (EmptyFileException e) {
            if (GlobalConfig.EMPTY_FILE_ERROR_CONSOLE) {
                System.err.println(e.getMessage());
            }
        }

        return jsonArray;
    }

    // Haal de data van de user uit een JSONObject, maak een user aan met de data en return deze.
    private static User getUserFromJson(JSONObject obj) {
        String id = obj.getString("id");
        String username = obj.getString("username");
        String password = obj.getString("password");
        UserType userType = UserType.getUserTypeFromType(obj.getString("userType"));
        Language preferredLanguage = Language.getLanguageFromCode(obj.getString("preferredLanguage"));

        if (userType.equals(UserType.EMPLOYEE)) {
            return new Employee(id, username, password, preferredLanguage);
        } else {
            return new Customer(id, username, password, preferredLanguage);
        }
    }

    // Maak een JSONObject aan met data van de user
    private static JSONObject getUserJson(User user) {
        JSONObject userJson = new JSONObject();
        userJson.put("id", user.getId());
        userJson.put("username", user.getUsername());
        userJson.put("password", user.getPassword());
        userJson.put("userType", user.getUserType().getType());
        userJson.put("preferredLanguage", user.getPreferredLanguage().getCode());
        return userJson;
    }

    // Haal de data van een product uit een JSONObject, maak een product aan met de data en return deze.
    private static Product getProductFromJson(JSONObject obj) {
        String id = obj.getString("id");
        String name = obj.getString("name");
        boolean isAvailable = obj.getBoolean("isAvailable");
        String ripeningDate = obj.getString("ripeningDate");
        int timesViewed = obj.getInt("timesViewed");
        String season = obj.getString("season");
        int stock = obj.getInt("stock");
        double price = obj.getDouble("price");

        return new Product(id, name, isAvailable, ripeningDate, timesViewed, season, stock, price);
    }

    // Maak een JSONObject aan met data van een product
    private static JSONObject getProductJson(Product product) {
        JSONObject productJson = new JSONObject();
        productJson.put("id", product.getId());
        productJson.put("name", product.getName());
        productJson.put("isAvailable", product.isAvailable());
        productJson.put("ripeningDate", product.getRipeningDate());
        productJson.put("timesViewed", product.getTimesViewed());
        productJson.put("season", product.getSeason());
        productJson.put("stock", product.getStock());
        productJson.put("price", product.getPrice());
        return productJson;
    }

    // Haal de data van een ShoppingListProduct uit een JSONObject, maak een ShoppingListProduct aan met de data en return deze.
    private static ShoppingListProduct getShoppingListProductFromJson(JSONObject obj) {
        String productId = obj.getString("productId");
        int amountInShoppinglist = obj.getInt("amountInShoppinglist");

        return new ShoppingListProduct(Inventory.findProductById(productId), amountInShoppinglist);
    }

    // Maak een JSONObject aan met data van een ShoppingListProduct
    private static JSONObject getShoppingListProductJson(ShoppingListProduct product) {
        JSONObject productJson = new JSONObject();
        productJson.put("productId", product.getId());
        productJson.put("amountInShoppinglist", product.getAmountInShoppingList());
        return productJson;
    }

    // Hulpfunctie om JSONObject voor een Branch te maken
    private static JSONObject createBranchJSONObject(Branch branch) {
        JSONObject branchObject = new JSONObject();
        branchObject.put("id", branch.getId());
        branchObject.put("name", branch.getName());
        branchObject.put("country", branch.getCountry());
        branchObject.put("city", branch.getCity());
        branchObject.put("postalCode", branch.getPostalCode());
        branchObject.put("street", branch.getStreet());
        branchObject.put("streetNumber", branch.getStreetNumber());
        return branchObject;
    }

    // Hulpfunctie om JSONObject voor BranchProduct te maken
    private static JSONObject createBranchProductJSONObject(BranchProduct product) {
        JSONObject productObject = new JSONObject();
        productObject.put("id", product.getId());
        productObject.put("name", product.getName());
        productObject.put("price", product.getPrice());
        productObject.put("stock", product.getStock());
        productObject.put("status", product.getStatus().getId());
        return productObject;
    }

    // Parsing van JSONObject naar Branch
    private static Branch parseBranchJSONObject(JSONObject branchObject) {
        String id = branchObject.getString("id");
        String name = branchObject.getString("name");
        String country = branchObject.getString("country");
        String city = branchObject.getString("city");
        String postalCode = branchObject.getString("postalCode");
        String street = branchObject.getString("street");
        int streetNumber = branchObject.getInt("streetNumber");

        return new Branch(id, name, country, city, postalCode, street, streetNumber);
    }

    // Parsing van JSONObject naar BranchProduct
    private static BranchProduct parseBranchProductJSONObject(JSONObject productObject) {
        ProductStatus status = ProductStatus.getById(productObject.getInt("status"));
        int stock = productObject.getInt("stock");
        String productId = productObject.getString("productId");

        return new BranchProduct(Inventory.findProductById(productId), stock, status);
    }
}
