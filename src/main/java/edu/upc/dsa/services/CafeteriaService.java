package edu.upc.dsa.services;

import edu.upc.dsa.domain.models.CafeteriaItem;
import edu.upc.dsa.domain.models.CafeteriaOrder;
import edu.upc.dsa.domain.models.User;
import edu.upc.dsa.domain.repository.GameManager;
import edu.upc.dsa.domain.repository.GameManagerImpl;
import edu.upc.dsa.exceptions.BadRequestException;
import edu.upc.dsa.exceptions.NotFoundException;
import edu.upc.dsa.exceptions.UnauthorizedException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import javax.ws.rs.*;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 - Servei REST per gestionar el menú i les compres a la cafeteria de l'EETAC.
 - Ruta base: /api/cafeteria
 */
@Api(value = "/api/cafeteria", description = "Endpoint for EETAC Cafeteria Menu and Purchases")
@Path("/api/cafeteria")
public class CafeteriaService {

    private GameManager gm;

    public CafeteriaService() {
        this.gm = GameManagerImpl.getInstance();
    }

    /**
     - PETICIÓ: GET /api/cafeteria/menu
     - QUÈ FA: Obté la llista completa de productes disponibles a la cafeteria.
     - DEMANA: Res (no requereix paràmetres ni Body).
     - RETORNA:
     -   - 200 OK: Llista en format JSON d'objectes CafeteriaItem (idItem, name, priceCoins, energyRecovery).
     */
    @GET
    @Path("/menu")
    @ApiOperation(value = "Get cafeteria menu", notes = "Returns all available food and drinks at EETAC cafeteria")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful", response = CafeteriaItem.class, responseContainer = "List")
    })
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMenu() {
        List<CafeteriaItem> items = this.gm.getCafeteriaCatalog();
        GenericEntity<List<CafeteriaItem>> entity = new GenericEntity<List<CafeteriaItem>>(items) {};
        return Response.status(200).entity(entity).build();
    }

    /**
     - PETICIÓ: POST /api/cafeteria/compres
     - QUÈ FA: Processa la compra d'un producte per a l'estudiant autenticat mitjançant el seu token.
     - DEMANA:
     -   - Header: Authorization: Bearer <token>
     -   - Body JSON: Objecte CafeteriaOrder amb { "producteId": "..." } (el userId l'omple el servidor).
     - RETORNA:
     -   - 200 OK: L'objecte User actualitzat amb les noves monedes i la nova energia (màxim 100).
     -   - 400 Bad Request: Si falta el producteId o l'estudiant no té prou monedes.
     -   - 401 Unauthorized: Si falta la capçalera Authorization o el token no és vàlid.
     -   - 404 Not Found: Si el producteId no existeix al catàleg de la cafeteria.
     */
    @POST
    @Path("/compres")
    @ApiOperation(value = "Buy a cafeteria item", notes = "Identifies student via Bearer token and processes the CafeteriaOrder")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Purchase successful", response = User.class),
            @ApiResponse(code = 400, message = "Bad Request (Insufficient coins or missing producteId)"),
            @ApiResponse(code = 401, message = "Unauthorized (Missing or invalid token)"),
            @ApiResponse(code = 404, message = "Not Found (CafeteriaItem does not exist)")
    })
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response buyItem(@HeaderParam("Authorization") String authHeader, CafeteriaOrder order) {
        try {
            // 1. Identifiquem l'usuari real a partir del seu token de sessió
            User user = this.gm.getUserByToken(authHeader);

            if (order == null || order.getProducteId() == null || order.getProducteId().trim().isEmpty()) {
                return Response.status(400).entity("producteId is required in the JSON body.").build();
            }

            // 2. Assignem de forma segura l'ID real de l'usuari al teu CafeteriaOrder
            order.setUserId(user.getIdUser());

            // 3. El GameManager processa la compra amb els IDs interns
            User updatedUser = this.gm.buyCafeteriaItem(order);
            return Response.status(200).entity(updatedUser).build();

        } catch (UnauthorizedException e) {
            return Response.status(401).entity(e.getMessage()).build();
        } catch (NotFoundException e) {
            return Response.status(404).entity(e.getMessage()).build();
        } catch (BadRequestException e) {
            return Response.status(400).entity(e.getMessage()).build();
        }
    }
}