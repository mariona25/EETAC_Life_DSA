package edu.upc.dsa.services;

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
 * Servei REST per gestionar l'autenticació i el perfil dels estudiants.
 * Ruta base: /api/estudiants
 */
@Api(value = "/api/estudiants", description = "Endpoint for Student Authentication and Profile Management")
@Path("/api/estudiants")
public class EstudiantsService {

    private GameManager gm;

    public EstudiantsService() {
        this.gm = GameManagerImpl.getInstance();
    }

    /**
     * PETICIÓ: POST /api/estudiants/registre
     * QUÈ FA: Registra un nou estudiant al joc.
     * DEMANA (Body JSON): Objecte Credentials amb { "username", "email", "password" }.
     * RETORNA:
     *   - 201 Created: L'objecte User creat (amb el seu idUser generat, monedes i energia inicials).
     *   - 400 Bad Request: Si falta algun camp obligatori o ve buit.
     *   - 409 Conflict: Si el username o l'email ja estan registrats per un altre usuari.
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
     * PETICIÓ: POST /api/estudiants/login
     * QUÈ FA: Valida les credencials d'un estudiant per iniciar sessió.
     * DEMANA (Body JSON): Objecte Credentials amb { "username", "password" } (l'email no cal).
     * RETORNA:
     *   - 200 OK: L'objecte User complet amb totes les seves estadístiques actuals.
     *   - 400 Bad Request: Si falta el username o la contrasenya.
     *   - 401 Unauthorized: Si l'usuari existeix però la contrasenya és incorrecta.
     *   - 404 Not Found: Si no existeix cap estudiant amb aquest username.
     */
    @POST
    @Path("/login")
    @ApiOperation(value = "Student login", notes = "Authenticates a student using username and password")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful", response = User.class),
            @ApiResponse(code = 400, message = "Bad Request (Missing credentials)"),
            @ApiResponse(code = 401, message = "Unauthorized (Invalid password)"),
            @ApiResponse(code = 404, message = "Not Found (User does not exist)")
    })
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(Credentials credentials) {
        try {
            User user = this.gm.loginUser(credentials);
            return Response.status(200).entity(user).build();
        } catch (BadRequestException e) {
            return Response.status(400).entity(e.getMessage()).build();
        } catch (NotFoundException e) {
            return Response.status(404).entity(e.getMessage()).build();
        } catch (UnauthorizedException e) {
            return Response.status(401).entity(e.getMessage()).build();
        }
    }

    /**
     * PETICIÓ: GET /api/estudiants/{username}
     * QUÈ FA: Consulta el perfil i l'estat actual d'un estudiant pel seu nom d'usuari.
     * DEMANA (Path Param): El username de l'estudiant a la pròpia URL (ex: /api/estudiants/marc).
     * RETORNA:
     *   - 200 OK: L'objecte User (idUser, monedes, energia, ECTS, minijocs superats...).
     *   - 404 Not Found: Si no hi ha cap estudiant amb aquest username.
     */
    @GET
    @Path("/{username}")
    @ApiOperation(value = "Get student profile by username", notes = "Returns student stats (coins, energy, ECTS, etc.)")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful", response = User.class),
            @ApiResponse(code = 404, message = "Not Found (User does not exist)")
    })
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUser(@PathParam("username") String username) {
        try {
            User user = this.gm.getUserByUsername(username);
            return Response.status(200).entity(user).build();
        } catch (NotFoundException e) {
            return Response.status(404).entity(e.getMessage()).build();
        }
    }

    /**
     * PETICIÓ: DELETE /api/estudiants/{username}?password=...
     * QUÈ FA: Elimina permanentment el compte d'un estudiant, verificant abans la seva contrasenya.
     * DEMANA:
     *   - Path Param: El username de l'estudiant a la URL (ex: /api/estudiants/marc).
     *   - Query Param: La contrasenya de l'estudiant (?password=...) per confirmar que és el seu propi compte.
     * RETORNA:
     *   - 200 OK: Missatge confirmant que el compte s'ha esborrat.
     *   - 400 Bad Request: Si no s'ha enviat el paràmetre password.
     *   - 401 Unauthorized: Si la contrasenya és incorrecta (evita que algú esborri el compte d'un altre).
     *   - 404 Not Found: Si no existeix cap estudiant amb aquest username.
     */
    @DELETE
    @Path("/{username}")
    @ApiOperation(value = "Delete own student account", notes = "Removes a student from the system after verifying password")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Deleted successfully"),
            @ApiResponse(code = 400, message = "Bad Request (Password required)"),
            @ApiResponse(code = 401, message = "Unauthorized (Invalid password)"),
            @ApiResponse(code = 404, message = "Not Found (User does not exist)")
    })
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteUser(@PathParam("username") String username, @QueryParam("password") String password) {
        try {
            this.gm.deleteUserByUsername(username, password);
            return Response.status(200).entity("User '" + username + "' deleted successfully.").build();
        } catch (BadRequestException e) {
            return Response.status(400).entity(e.getMessage()).build();
        } catch (UnauthorizedException e) {
            return Response.status(401).entity(e.getMessage()).build();
        } catch (NotFoundException e) {
            return Response.status(404).entity(e.getMessage()).build();
        }
    }
}