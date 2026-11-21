package edu.upc.dsa.domain.repository;

import edu.upc.dsa.domain.models.*;
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
    private Map<String, String> sessions; // Maps session token -> idUser

    // Private constructor (Singleton pattern): initializes storage maps and loads the cafeteria menu
    private GameManagerImpl() {
        this.users = new HashMap<>();
        this.cafeteriaItems = new HashMap<>();
        this.sessions = new HashMap<>();
        this.initCafeteriaItems();
    }

    // Returns the single shared instance of GameManagerImpl across the entire backend
    public static GameManager getInstance() {
        if (instance == null) {
            instance = new GameManagerImpl();
        }
        return instance;
    }

    // Reads resources/cafeteria.txt line by line and populates the cafeteriaItems catalog
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
                        isFirstLine = false; // Skip header line
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
    // 1. LOOKUP & TRANSLATION METHODS
    // =========================================================================

    // Finds and returns the internal idUser matching a given username
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

    // Finds and returns the internal idItem matching a cafeteria product's name
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
    // 2. STUDENT MANAGEMENT & AUTHENTICATION
    // =========================================================================

    // Validates input fields, checks for duplicate username/email, creates a new User, and stores it
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

    // Verifies that the username exists and matches the provided password, returning the User
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

    // Retrieves a student's User profile using their username
    @Override
    public User getUserByUsername(String username) throws NotFoundException {
        logger.info("Fetching profile for username: " + username);
        String idUser = this.getUserIdByUsername(username);
        return this.users.get(idUser);
    }

    // Authenticates the user, generates a unique session token, stores the session, and returns AuthResponse
    @Override
    public AuthResponse loginWithToken(Credentials credentials) throws BadRequestException, NotFoundException, UnauthorizedException {
        User foundUser = this.loginUser(credentials);
        String token = java.util.UUID.randomUUID().toString();
        this.sessions.put(token, foundUser.getIdUser());
        logger.info("Token generated for user " + foundUser.getUsername() + ": " + token);
        return new AuthResponse(token, foundUser);
    }

    // Extracts the Bearer token from the Authorization header and returns the corresponding authenticated User
    @Override
    public User getUserByToken(String authHeader) throws UnauthorizedException {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Missing or invalid Authorization header. Expected: 'Bearer <token>'");
        }
        String token = authHeader.substring(7).trim();
        String idUser = this.sessions.get(token);

        if (idUser == null || !this.users.containsKey(idUser)) {
            throw new UnauthorizedException("Invalid or expired session token.");
        }
        return this.users.get(idUser);
    }

    // Validates the session token, deletes the user account from storage, and closes all active sessions for that user
    @Override
    public void deleteUserByToken(String authHeader) throws UnauthorizedException {
        User userToDelete = this.getUserByToken(authHeader);
        String idUser = userToDelete.getIdUser();

        this.users.remove(idUser);
        this.sessions.values().removeIf(id -> id.equals(idUser));
        logger.info("User deleted via token: " + userToDelete.getUsername());
    }

    // =========================================================================
    // 3. CAMPUS CAFETERIA
    // =========================================================================

    // Returns the full list of available food and drink items in the cafeteria
    @Override
    public List<CafeteriaItem> getCafeteriaCatalog() {
        logger.info("Fetching cafeteria catalog (" + this.cafeteriaItems.size() + " items).");
        return new ArrayList<>(this.cafeteriaItems.values());
    }

    // Processes a cafeteria purchase: checks balance, deducts coins, restores energy, and returns updated User
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
        int updatedEnergy = Math.min(100, user.getEnergy() + item.getEnergyRecovery()); //MIRAR QUINA ENERGIA MÀXIMA VOLEM POSAR
        user.setEnergy(updatedEnergy);

        logger.info("Purchase successful: " + user.getUsername() + " bought " + item.getName()
                + ". Remaining coins: " + user.getCoins() + ", current energy: " + user.getEnergy());
        return user;
    }

    // =========================================================================
    // 4. UTILITY & TESTING METHODS
    // =========================================================================

    // Returns the total number of registered users
    @Override
    public int numUsers() {
        return this.users.size();
    }

    // Returns the total number of items in the cafeteria catalog
    @Override
    public int numCafeteriaItems() {
        return this.cafeteriaItems.size();
    }

    // Resets all stored users, sessions, and reloads the initial cafeteria catalog (used in JUnit tests)
    @Override
    public void clear() {
        this.users.clear();
        this.cafeteriaItems.clear();
        this.initCafeteriaItems();
        this.sessions.clear();
        logger.info("GameManager state cleared and cafeteria items re-initialized.");
    }
}