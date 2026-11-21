package edu.upc.dsa.domain.repository;

import edu.upc.dsa.domain.models.*;
import edu.upc.dsa.exceptions.BadRequestException;
import edu.upc.dsa.exceptions.ConflictException;
import edu.upc.dsa.exceptions.NotFoundException;
import edu.upc.dsa.exceptions.UnauthorizedException;

import java.util.List;

public interface GameManager {

    // 1. GESTIÓ D'ESTUDIANTS (Auth i Perfil)
    User registerUser(Credentials credentials) throws BadRequestException, ConflictException;
    User loginUser(Credentials credentials) throws BadRequestException, NotFoundException, UnauthorizedException;
    User getUserByUsername(String username) throws NotFoundException;
    AuthResponse loginWithToken(Credentials credentials) throws BadRequestException, NotFoundException, UnauthorizedException;
    User getUserByToken(String authHeader) throws UnauthorizedException;
    void deleteUserByToken(String authHeader) throws UnauthorizedException;

    // 2. TRADUCCIÓ NOM -> ID INTERN
    String getUserIdByUsername(String username) throws NotFoundException;
    String getCafeteriaItemIdByName(String productName) throws NotFoundException;

    // 3. CAFETERIA DEL CAMPUS
    List<CafeteriaItem> getCafeteriaCatalog();
    User buyCafeteriaItem(CafeteriaOrder order) throws NotFoundException, BadRequestException;

    // 4. MÈTODES AUXILIARS (Per a tests JUnit i inicialització)
    int numUsers();
    int numCafeteriaItems();
    void clear();
}