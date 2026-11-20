package edu.upc.dsa.domain.repository;

import edu.upc.dsa.domain.models.CafeteriaItem;
import edu.upc.dsa.domain.models.CafeteriaOrder;
import edu.upc.dsa.domain.models.Credentials;
import edu.upc.dsa.domain.models.User;
import edu.upc.dsa.exceptions.BadRequestException;
import edu.upc.dsa.exceptions.ConflictException;
import edu.upc.dsa.exceptions.NotFoundException;
import edu.upc.dsa.exceptions.UnauthorizedException;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameManagerImpl implements GameManager {

    private static GameManager instance;
    private static final Logger logger = Logger.getLogger(GameManagerImpl.class);

    private Map<String, User> users;
    private Map<String, CafeteriaItem> cafeteriaItems;

    private GameManagerImpl() {
        this.users = new HashMap<>();
        this.cafeteriaItems = new HashMap<>();
        this.initCafeteriaItems();
    }

    public static GameManager getInstance() {
        if (instance == null) {
            instance = new GameManagerImpl();
        }
        return instance;
    }

    private void initCafeteriaItems() {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("cafeteria.txt")) {
            if (is == null) {
                logger.error("cafeteria.txt not found in resources! Loading default fallback item.");
                this.cafeteriaItems.put("caf_1", new CafeteriaItem("caf_1", "Cafè amb llet", 10, 15));
                return;
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String line;
                boolean isFirstLine = true;

                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#")) {
                        continue;
                    }
                    if (isFirstLine) {
                        isFirstLine = false;
                        continue;
                    }

                    String[] parts = line.split(";");
                    if (parts.length == 4) {
                        String id = parts[0].trim();
                        String name = parts[1].trim();
                        int price = Integer.parseInt(parts[2].trim());
                        int energy = Integer.parseInt(parts[3].trim());

                        this.cafeteriaItems.put(id, new CafeteriaItem(id, name, price, energy));
                    } else {
                        logger.warn("Skipping malformed line in cafeteria.txt: " + line);
                    }
                }
            }
            logger.info("Cafeteria catalog initialized from cafeteria.txt with " + this.cafeteriaItems.size() + " items.");
        } catch (Exception e) {
            logger.error("Error reading cafeteria.txt", e);
        }
    }

    // =========================================================================
    // 1. MÈTODES DE TRADUCCIÓ (Nom humà -> ID intern)
    // =========================================================================

    @Override
    public String getUserIdByUsername(String username) throws NotFoundException {
        if (username == null || username.trim().isEmpty()) {
            throw new NotFoundException("Username cannot be empty.");
        }
        for (User user : this.users.values()) {
            if (user.getUsername().equalsIgnoreCase(username.trim())) {
                return user.getIdUser();
            }
        }
        throw new NotFoundException("User '" + username + "' not found.");
    }

    @Override
    public String getCafeteriaItemIdByName(String productName) throws NotFoundException {
        if (productName == null || productName.trim().isEmpty()) {
            throw new NotFoundException("Product name cannot be empty.");
        }
        for (CafeteriaItem item : this.cafeteriaItems.values()) {
            if (item.getName().equalsIgnoreCase(productName.trim())) {
                return item.getIdItem();
            }
        }
        throw new NotFoundException("Cafeteria item '" + productName + "' not found.");
    }

    // =========================================================================
    // 2. GESTIÓ D'ESTUDIANTS
    // =========================================================================

    @Override
    public User registerUser(Credentials credentials) throws BadRequestException, ConflictException {
        logger.info("Attempting to register user: " + (credentials != null ? credentials.getUsername() : "null"));

        if (credentials == null || credentials.getUsername() == null || credentials.getUsername().trim().isEmpty()
                || credentials.getPassword() == null || credentials.getPassword().trim().isEmpty()
                || credentials.getEmail() == null || credentials.getEmail().trim().isEmpty()) {
            logger.warn("Registration failed: missing required fields.");
            throw new BadRequestException("Username, email, and password are required.");
        }

        for (User existingUser : this.users.values()) {
            if (existingUser.getUsername().equalsIgnoreCase(credentials.getUsername().trim())) {
                logger.warn("Registration failed: username already exists -> " + credentials.getUsername());
                throw new ConflictException("Username '" + credentials.getUsername() + "' is already taken.");
            }
            if (existingUser.getEmail().equalsIgnoreCase(credentials.getEmail().trim())) {
                logger.warn("Registration failed: email already exists -> " + credentials.getEmail());
                throw new ConflictException("Email '" + credentials.getEmail() + "' is already registered.");
            }
        }

        User newUser = new User(
                credentials.getUsername().trim(),
                credentials.getEmail().trim(),
                credentials.getPassword()
        );

        this.users.put(newUser.getIdUser(), newUser);
        logger.info("User registered successfully: " + newUser.getUsername() + " (ID: " + newUser.getIdUser() + ")");
        return newUser;
    }

    @Override
    public User loginUser(Credentials credentials) throws BadRequestException, NotFoundException, UnauthorizedException {
        logger.info("Attempting login for user: " + (credentials != null ? credentials.getUsername() : "null"));

        if (credentials == null || credentials.getUsername() == null || credentials.getUsername().trim().isEmpty()
                || credentials.getPassword() == null || credentials.getPassword().trim().isEmpty()) {
            logger.warn("Login failed: missing username or password.");
            throw new BadRequestException("Username and password are required.");
        }

        String idUser = this.getUserIdByUsername(credentials.getUsername());
        User foundUser = this.users.get(idUser);

        if (!foundUser.getPassword().equals(credentials.getPassword())) {
            logger.warn("Login failed: invalid password for user -> " + credentials.getUsername());
            throw new UnauthorizedException("Invalid password.");
        }

        logger.info("User logged in successfully: " + foundUser.getUsername());
        return foundUser;
    }

    @Override
    public User getUserByUsername(String username) throws NotFoundException {
        logger.info("Fetching profile for username: " + username);
        String idUser = this.getUserIdByUsername(username);
        return this.users.get(idUser);
    }

    @Override
    public void deleteUserByUsername(String username, String password) throws BadRequestException, NotFoundException, UnauthorizedException {
        logger.info("Attempting to delete user with username: " + username);

        if (password == null || password.trim().isEmpty()) {
            logger.warn("Delete failed: password is required.");
            throw new BadRequestException("Password is required to delete the account.");
        }

        String idUser = this.getUserIdByUsername(username);
        User userToDelete = this.users.get(idUser);

        if (!userToDelete.getPassword().equals(password)) {
            logger.warn("Delete failed: invalid password for user -> " + username);
            throw new UnauthorizedException("Invalid password. You can only delete your own account.");
        }

        this.users.remove(idUser);
        logger.info("User deleted successfully: " + userToDelete.getUsername() + " (ID: " + idUser + ")");
    }

    // =========================================================================
    // 3. CAFETERIA DEL CAMPUS (Treballa amb CafeteriaOrder i IDs reals)
    // =========================================================================

    @Override
    public List<CafeteriaItem> getCafeteriaCatalog() {
        logger.info("Fetching cafeteria catalog (" + this.cafeteriaItems.size() + " items).");
        return new ArrayList<>(this.cafeteriaItems.values());
    }

    @Override
    public User buyCafeteriaItem(CafeteriaOrder order) throws NotFoundException, BadRequestException {
        if (order == null || order.getUserId() == null || order.getProducteId() == null) {
            throw new BadRequestException("Order with valid userId and producteId is required.");
        }

        logger.info("User ID '" + order.getUserId() + "' attempting to buy item ID '" + order.getProducteId() + "'");

        User user = this.users.get(order.getUserId());
        if (user == null) {
            logger.warn("Purchase failed: user not found -> " + order.getUserId());
            throw new NotFoundException("User with ID '" + order.getUserId() + "' not found.");
        }

        CafeteriaItem item = this.cafeteriaItems.get(order.getProducteId());
        if (item == null) {
            logger.warn("Purchase failed: cafeteria item not found -> " + order.getProducteId());
            throw new NotFoundException("Cafeteria item '" + order.getProducteId() + "' not found.");
        }

        if (user.getCoins() < item.getPriceCoins()) {
            logger.warn("Purchase failed: insufficient coins for user " + user.getUsername()
                    + " (has " + user.getCoins() + ", needs " + item.getPriceCoins() + ")");
            throw new BadRequestException("Insufficient coins. Required: " + item.getPriceCoins() + ", available: " + user.getCoins());
        }

        user.setCoins(user.getCoins() - item.getPriceCoins());
        int updatedEnergy = Math.min(100, user.getEnergy() + item.getEnergyRecovery());
        user.setEnergy(updatedEnergy);

        logger.info("Purchase successful: " + user.getUsername() + " bought " + item.getName()
                + ". Remaining coins: " + user.getCoins() + ", current energy: " + user.getEnergy());
        return user;
    }

    @Override
    public int numUsers() {
        return this.users.size();
    }

    @Override
    public int numCafeteriaItems() {
        return this.cafeteriaItems.size();
    }

    @Override
    public void clear() {
        this.users.clear();
        this.cafeteriaItems.clear();
        this.initCafeteriaItems();
        logger.info("GameManager state cleared and cafeteria items re-initialized.");
    }
}