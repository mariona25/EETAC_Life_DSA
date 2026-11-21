package edu.upc.dsa;

import edu.upc.dsa.domain.models.*;
import edu.upc.dsa.domain.repository.GameManager;
import edu.upc.dsa.domain.repository.GameManagerImpl;
import edu.upc.dsa.exceptions.BadRequestException;
import edu.upc.dsa.exceptions.ConflictException;
import edu.upc.dsa.exceptions.NotFoundException;
import edu.upc.dsa.exceptions.UnauthorizedException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class GameManagerTest {

    private GameManager gm;

    @Before
    public void setUp() throws Exception {
        this.gm = GameManagerImpl.getInstance();
        this.gm.clear();

        // Register a default test student before each test
        Credentials marioCredentials = new Credentials("mario", "mario@estudiantat.upc.edu", "1234");
        this.gm.registerUser(marioCredentials);
    }

    @After
    public void tearDown() {
        this.gm.clear();
    }

    // =========================================================================
    // 1. REGISTRATION TESTS & ERRORS
    // =========================================================================

    @Test
    public void testRegisterSuccess() throws Exception {
        Assert.assertEquals(1, this.gm.numUsers());

        Credentials annaCredentials = new Credentials("anna", "anna@estudiantat.upc.edu", "pass1234");
        User anna = this.gm.registerUser(annaCredentials);

        Assert.assertNotNull(anna);
        Assert.assertNotNull(anna.getIdUser());
        Assert.assertEquals("anna", anna.getUsername());
        Assert.assertEquals(2, this.gm.numUsers());
    }

    @Test(expected = BadRequestException.class)
    public void testRegisterNullCredentials() throws Exception {
        this.gm.registerUser(null);
    }

    @Test(expected = BadRequestException.class)
    public void testRegisterMissingFields() throws Exception {
        // Missing password
        Credentials incomplete = new Credentials("pepe", "pepe@upc.edu", "");
        this.gm.registerUser(incomplete);
    }

    @Test(expected = BadRequestException.class)
    public void testRegisterWhitespaceOnlyFields() throws Exception {
        Credentials spaces = new Credentials("   ", "valid@upc.edu", "1234");
        this.gm.registerUser(spaces);
    }

    @Test(expected = ConflictException.class)
    public void testRegisterDuplicateUsername() throws Exception {
        // Case-insensitive duplicate check ("MARIO" vs "mario")
        Credentials duplicateUser = new Credentials("MARIO", "other@estudiantat.upc.edu", "9999");
        this.gm.registerUser(duplicateUser);
    }

    @Test(expected = ConflictException.class)
    public void testRegisterDuplicateEmail() throws Exception {
        Credentials duplicateEmail = new Credentials("student2", "MARIO@estudiantat.upc.edu", "9999");
        this.gm.registerUser(duplicateEmail);
    }

    // =========================================================================
    // 2. LOGIN & SESSION TOKEN TESTS & ERRORS
    // =========================================================================

    @Test
    public void testLoginAndTokenSessionFlow() throws Exception {
        Credentials loginCredentials = new Credentials("mario", null, "1234");
        AuthResponse auth = this.gm.loginWithToken(loginCredentials);

        Assert.assertNotNull(auth);
        Assert.assertNotNull(auth.getToken());
        Assert.assertEquals("mario", auth.getUser().getUsername());

        // Verify token works with "Bearer <token>" header
        String authHeader = "Bearer " + auth.getToken();
        User me = this.gm.getUserByToken(authHeader);
        Assert.assertEquals("mario", me.getUsername());
        Assert.assertEquals(auth.getUser().getIdUser(), me.getIdUser());
    }

    @Test(expected = BadRequestException.class)
    public void testLoginMissingFields() throws Exception {
        Credentials emptyPass = new Credentials("mario", null, "");
        this.gm.loginWithToken(emptyPass);
    }

    @Test(expected = NotFoundException.class)
    public void testLoginUserNotFound() throws Exception {
        Credentials unknown = new Credentials("ghost_student", null, "1234");
        this.gm.loginWithToken(unknown);
    }

    @Test(expected = UnauthorizedException.class)
    public void testLoginWrongPassword() throws Exception {
        Credentials wrongPass = new Credentials("mario", null, "wrong_password");
        this.gm.loginWithToken(wrongPass);
    }

    @Test(expected = UnauthorizedException.class)
    public void testGetUserByTokenMissingBearerPrefix() throws Exception {
        AuthResponse auth = this.gm.loginWithToken(new Credentials("mario", null, "1234"));
        // Sending raw token without "Bearer " must fail
        this.gm.getUserByToken(auth.getToken());
    }

    @Test(expected = UnauthorizedException.class)
    public void testGetUserByTokenNullHeader() throws Exception {
        this.gm.getUserByToken(null);
    }

    @Test(expected = UnauthorizedException.class)
    public void testGetUserByInvalidToken() throws Exception {
        this.gm.getUserByToken("Bearer fake-token-12345");
    }

    @Test
    public void testDeleteAccountInvalidatesAllSessions() throws Exception {
        // Simulate logging in from two devices (e.g., Android + Web)
        AuthResponse mobileSession = this.gm.loginWithToken(new Credentials("mario", null, "1234"));
        AuthResponse webSession = this.gm.loginWithToken(new Credentials("mario", null, "1234"));

        String mobileHeader = "Bearer " + mobileSession.getToken();
        String webHeader = "Bearer " + webSession.getToken();

        // Delete account from mobile
        this.gm.deleteUserByToken(mobileHeader);
        Assert.assertEquals(0, this.gm.numUsers());

        // Verify both mobile and web tokens are now rejected
        try {
            this.gm.getUserByToken(mobileHeader);
            Assert.fail("Expected UnauthorizedException for deleted user's mobile token");
        } catch (UnauthorizedException expected) {
            // OK
        }

        try {
            this.gm.getUserByToken(webHeader);
            Assert.fail("Expected UnauthorizedException for deleted user's web token");
        } catch (UnauthorizedException expected) {
            // OK
        }
    }

    // =========================================================================
    // 3. LOOKUP METHODS TESTS & ERRORS
    // =========================================================================

    @Test
    public void testLookupMethodsSuccess() throws Exception {
        String userId = this.gm.getUserIdByUsername("MARIO");
        User u = this.gm.getUserByUsername("mario");
        Assert.assertEquals(u.getIdUser(), userId);

        String itemId = this.gm.getCafeteriaItemIdByName("cafè amb llet");
        Assert.assertEquals("caf_1", itemId);
    }

    @Test(expected = NotFoundException.class)
    public void testLookupUnknownUser() throws Exception {
        this.gm.getUserIdByUsername("no_existeix");
    }

    @Test(expected = NotFoundException.class)
    public void testLookupUnknownCafeteriaItem() throws Exception {
        this.gm.getCafeteriaItemIdByName("Caviar d'Iran");
    }

    // =========================================================================
    // 4. CAFETERIA CATALOG & PURCHASES TESTS & ERRORS
    // =========================================================================

    @Test
    public void testCafeteriaCatalogLoaded() {
        List<CafeteriaItem> menu = this.gm.getCafeteriaCatalog();
        Assert.assertEquals(4, menu.size());
        Assert.assertEquals(4, this.gm.numCafeteriaItems());
    }

    @Test
    public void testBuyCafeteriaItemSuccessAndEnergyCap() throws Exception {
        User mario = this.gm.getUserByUsername("mario");
        mario.setCoins(100);
        mario.setEnergy(70);

        // Buy "Entrepà de fuet" (caf_2: price = 25, energyRecovery = 40)
        // Energy should cap at 100 (70 + 40 = 110 -> 100), coins should be 75
        CafeteriaOrder order = new CafeteriaOrder(mario.getIdUser(), "caf_2");
        User updatedMario = this.gm.buyCafeteriaItem(order);

        Assert.assertEquals(75, updatedMario.getCoins());
        Assert.assertEquals(100, updatedMario.getEnergy());
    }

    @Test(expected = BadRequestException.class)
    public void testBuyCafeteriaItemInsufficientCoins() throws Exception {
        User mario = this.gm.getUserByUsername("mario");
        mario.setCoins(5); // Not enough for caf_1 (price = 10)

        CafeteriaOrder order = new CafeteriaOrder(mario.getIdUser(), "caf_1");
        this.gm.buyCafeteriaItem(order);
    }

    @Test(expected = NotFoundException.class)
    public void testBuyCafeteriaItemProductNotFound() throws Exception {
        User mario = this.gm.getUserByUsername("mario");
        mario.setCoins(100);

        CafeteriaOrder order = new CafeteriaOrder(mario.getIdUser(), "caf_999");
        this.gm.buyCafeteriaItem(order);
    }

    @Test(expected = NotFoundException.class)
    public void testBuyCafeteriaItemUserNotFound() throws Exception {
        CafeteriaOrder order = new CafeteriaOrder("non-existent-user-id", "caf_1");
        this.gm.buyCafeteriaItem(order);
    }

    @Test(expected = BadRequestException.class)
    public void testBuyCafeteriaItemNullOrder() throws Exception {
        this.gm.buyCafeteriaItem(null);
    }
}