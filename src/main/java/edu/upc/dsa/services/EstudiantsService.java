package edu.upc.dsa.services;

import edu.upc.dsa.domain.models.AuthResponse;
import edu.upc.dsa.domain.models.Credentials;
import edu.upc.dsa.domain.models.User;
import edu.upc.dsa.domain.repository.GameManager;
import edu.upc.dsa.domain.repository.GameManagerImpl;
import edu.upc.dsa.exceptions.BadRequestException;
import edu.upc.dsa.exceptions.ConflictException;
import edu.upc.dsa.exceptions.NotFoundException;
import edu.upc.dsa.exceptions.UnauthorizedException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 - Servei REST per gestionar l'autenticació i el perfil dels estudiants.
 - Ruta base: /api/estudiants
 */
@Api(value = "/api/estudiants", description = "Endpoint for Student Authentication and Profile Management")
@Path("/api/estudiants")
public class EstudiantsService {

    private GameManager gm;

    public EstudiantsService() {
        this.gm = GameManagerImpl.getInstance();
    }

    /**
     - PETICIÓ: POST /api/estudiants/registre
     - QUÈ FA: Registra un nou estudiant al joc.
     - DEMANA (Body JSON): Objecte Credentials amb { "username", "email", "password" }.
     - RETORNA:
     - 201 Created: L'objecte User creat (amb el seu idUser generat, monedes i energia inicials).
     - 400 Bad Request: Si falta algun camp obligatori o ve buit.
     - 409 Conflict: Si el username o l'email ja estan registrats per un altre usuari.
     */
    @POST
    @Path("/registre")
    @ApiOperation(value = "Register a new student", notes = "Creates a new student account with initial coins and energy")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Created", response = User.class),
            @ApiResponse(code = 400, message = "Bad Request (Missing fields)"),
            @ApiResponse(code = 409, message = "Conflict (Username or email already exists)")
    })
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response register(Credentials credentials) {
        try {
            User user = this.gm.registerUser(credentials);
            return Response.status(201).entity(user).build();
        } catch (BadRequestException e) {
            return Response.status(400).entity(e.getMessage()).build();
        } catch (ConflictException e) {
            return Response.status(409).entity(e.getMessage()).build();
        }
    }

    /**
     - PETICIÓ: POST /api/estudiants/login
     - QUÈ FA: Valida les credencials d'un estudiant i genera un token de sessió únic.
     - DEMANA (Body JSON): Objecte Credentials amb { "username", "password" } (l'email no cal).
     - RETORNA:
     -   - 200 OK: Objecte AuthResponse amb el { "token": "...", "user": { ... } }.
     -   - 400 Bad Request: Si falta el username o la contrasenya.
     -   - 401 Unauthorized: Si l'usuari existeix però la contrasenya és incorrecta.
     -   - 404 Not Found: Si no existeix cap estudiant amb aquest username.
     */
    @POST
    @Path("/login")
    @ApiOperation(value = "Student login", notes = "Authenticates a student and returns a session Bearer token + User profile")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful", response = AuthResponse.class),
            @ApiResponse(code = 400, message = "Bad Request (Missing credentials)"),
            @ApiResponse(code = 401, message = "Unauthorized (Invalid password)"),
            @ApiResponse(code = 404, message = "Not Found (User does not exist)")
    })
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(Credentials credentials) {
        try {
            AuthResponse authResponse = this.gm.loginWithToken(credentials);
            return Response.status(200).entity(authResponse).build();
        } catch (BadRequestException e) {
            return Response.status(400).entity(e.getMessage()).build();
        } catch (NotFoundException e) {
            return Response.status(404).entity(e.getMessage()).build();
        } catch (UnauthorizedException e) {
            return Response.status(401).entity(e.getMessage()).build();
        }
    }

    /**
     - PETICIÓ: GET /api/estudiants/me
     - QUÈ FA: Consulta el perfil i l'estat actual de l'estudiant autenticat mitjançant el seu token.
     - DEMANA (Header): Authorization: Bearer <token>
     - RETORNA:
     -   - 200 OK: L'objecte User (idUser, monedes, energia, ECTS, minijocs superats...).
     -   - 401 Unauthorized: Si falta la capçalera Authorization o el token no és vàlid.
     */
    @GET
    @Path("/me")
    @ApiOperation(value = "Get current student profile", notes = "Returns student stats (coins, energy, ECTS, etc.) using the Bearer token")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful", response = User.class),
            @ApiResponse(code = 401, message = "Unauthorized (Missing or invalid token)")
    })
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMe(@HeaderParam("Authorization") String authHeader) {
        try {
            User user = this.gm.getUserByToken(authHeader);
            return Response.status(200).entity(user).build();
        } catch (UnauthorizedException e) {
            return Response.status(401).entity(e.getMessage()).build();
        }
    }

    /**
     - PETICIÓ: DELETE /api/estudiants/me
     - QUÈ FA: Elimina permanentment el compte de l'estudiant autenticat i tanca la seva sessió.
     - DEMANA (Header): Authorization: Bearer <token>
     - RETORNA:
     -   - 200 OK: Missatge confirmant que el compte s'ha esborrat.
     -   - 401 Unauthorized: Si falta la capçalera Authorization o el token no és vàlid.
     */
    @DELETE
    @Path("/me")
    @ApiOperation(value = "Delete own student account", notes = "Removes the authenticated student from the system")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Deleted successfully"),
            @ApiResponse(code = 401, message = "Unauthorized (Missing or invalid token)")
    })
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteMe(@HeaderParam("Authorization") String authHeader) {
        try {
            this.gm.deleteUserByToken(authHeader);
            return Response.status(200).entity("Account deleted successfully.").build();
        } catch (UnauthorizedException e) {
            return Response.status(401).entity(e.getMessage()).build();
        }
    }
}