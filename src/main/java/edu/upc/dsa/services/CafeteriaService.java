package edu.upc.dsa.services;

import edu.upc.dsa.domain.models.CafeteriaItem;
import edu.upc.dsa.domain.models.CafeteriaOrder;
import edu.upc.dsa.domain.models.User;
import edu.upc.dsa.domain.repository.GameManager;
import edu.upc.dsa.domain.repository.GameManagerImpl;
import edu.upc.dsa.exceptions.BadRequestException;
import edu.upc.dsa.exceptions.NotFoundException;
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
 * Servei REST per gestionar el catàleg i les compres a la cafeteria de l'EETAC.
 * Ruta base: /api/cafeteria
 */
@Api(value = "/api/cafeteria", description = "Endpoint for EETAC Cafeteria Catalog and Purchases")
@Path("/api/cafeteria")
public class CafeteriaService {

    private GameManager gm;

    public CafeteriaService() {
        this.gm = GameManagerImpl.getInstance();
    }

    /**
     * PETICIÓ: GET /api/cafeteria/items
     * QUÈ FA: Obté la llista completa de productes disponibles a la cafeteria.
     * DEMANA: Res (no requereix paràmetres ni Body).
     * RETORNA:
     *   - 200 OK: Llista en format JSON d'objectes CafeteriaItem (idItem, name, priceCoins, energyRecovery).
     */
    @GET
    @Path("/items")
    @ApiOperation(value = "Get cafeteria catalog", notes = "Returns all available food and drinks at EETAC cafeteria")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful", response = CafeteriaItem.class, responseContainer = "List")
    })
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCatalog() {
        List<CafeteriaItem> items = this.gm.getCafeteriaCatalog();
        GenericEntity<List<CafeteriaItem>> entity = new GenericEntity<List<CafeteriaItem>>(items) {};
        return Response.status(200).entity(entity).build();
    }

    /**
     * PETICIÓ: POST /api/cafeteria/buy?username=...&productName=...
     * QUÈ FA: Processa la compra d'un producte a partir dels noms que envia l'usuari.
     *         Tradueix username -> userId i productName -> producteId per crear CafeteriaOrder.
     * DEMANA (Query Params): username i productName.
     * RETORNA:
     *   - 200 OK: L'objecte User actualitzat amb les noves monedes i la nova energia (màxim 100).
     *   - 400 Bad Request: Si falten paràmetres o l'estudiant no té prou monedes.
     *   - 404 Not Found: Si el username o el productName no existeixen al sistema.
     */
    @POST
    @Path("/buy")
    @ApiOperation(value = "Buy a cafeteria item", notes = "Receives username and productName, translates them to IDs, and processes the CafeteriaOrder")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Purchase successful", response = User.class),
            @ApiResponse(code = 400, message = "Bad Request (Insufficient coins or missing parameters)"),
            @ApiResponse(code = 404, message = "Not Found (User or CafeteriaItem does not exist)")
    })
    @Produces(MediaType.APPLICATION_JSON)
    public Response buyItem(@QueryParam("username") String username, @QueryParam("productName") String productName) {
        if (username == null || username.trim().isEmpty() || productName == null || productName.trim().isEmpty()) {
            return Response.status(400).entity("Both username and productName are required.").build();
        }
        try {
            // 1. Traduïm els noms que passa l'usuari als IDs interns
            String userId = this.gm.getUserIdByUsername(username);
            String producteId = this.gm.getCafeteriaItemIdByName(productName);

            // 2. Muntem el teu CafeteriaOrder intacte amb els IDs
            CafeteriaOrder order = new CafeteriaOrder(userId, producteId);

            // 3. El GameManager processa la compra amb els IDs
            User updatedUser = this.gm.buyCafeteriaItem(order);
            return Response.status(200).entity(updatedUser).build();
        } catch (NotFoundException e) {
            return Response.status(404).entity(e.getMessage()).build();
        } catch (BadRequestException e) {
            return Response.status(400).entity(e.getMessage()).build();
        }
    }
}