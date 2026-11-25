package edu.upc.dsa.eetaclife;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import edu.upc.dsa.eetaclife.api.ApiErrors;
import edu.upc.dsa.eetaclife.api.RetrofitClient;
import edu.upc.dsa.eetaclife.api.StudentProfileSource;
import edu.upc.dsa.eetaclife.models.CafeteriaItem;
import edu.upc.dsa.eetaclife.models.CafeteriaOrder;
import edu.upc.dsa.eetaclife.models.User;
import edu.upc.dsa.eetaclife.session.SessionStore;
import edu.upc.dsa.eetaclife.ui.StudentStats;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TiendaActivity extends AppCompatActivity {

    private LinearLayout catalog;
    private View stats;
    private TextView menuStatus;
    private TextView profileStatus;

    private Call<List<CafeteriaItem>> catalogCall;
    private Call<User> profileCall;
    private Call<User> purchaseCall;

    private final List<Button> purchaseButtons = new ArrayList<>();
    private boolean purchasing;
    private boolean profileLoaded;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tienda);

        catalog = findViewById(R.id.tiendaCatalog);
        stats = findViewById(R.id.statsTienda);
        menuStatus = findViewById(R.id.tvTiendaMenuStatus);
        profileStatus = findViewById(R.id.tvTiendaProfileStatus);

        findViewById(R.id.btnTiendaBack).setOnClickListener(v -> finish());
        loadCatalog();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (profileCall != null) {
            profileCall.cancel();
        }

        profileLoaded = false;
        setPurchaseEnabled(false);
        profileStatus.setText(R.string.loading_profile);

        profileCall = StudentProfileSource.load(this, new StudentProfileSource.Listener() {
            @Override
            public void onLoaded(User user) {
                StudentStats.render(stats, user);
                profileLoaded = true;
                setPurchaseEnabled(!purchasing);
                profileStatus.setText("");
            }

            @Override
            public void onError(String message) {
                stats.setVisibility(View.GONE);
                profileStatus.setText(message);
            }
        });
    }

    private void loadCatalog() {
        menuStatus.setText(R.string.loading_menu);
        catalogCall = RetrofitClient.getApiService().getCafeteriaMenu();

        catalogCall.enqueue(new Callback<List<CafeteriaItem>>() {
            @Override
            public void onResponse(Call<List<CafeteriaItem>> call,
                                   Response<List<CafeteriaItem>> response) {
                if (!response.isSuccessful()) {
                    menuStatus.setText(ApiErrors.describe(TiendaActivity.this, response));
                    return;
                }

                if (response.body() == null) {
                    menuStatus.setText(R.string.error_empty_response);
                    return;
                }

                renderCatalog(response.body());
            }

            @Override
            public void onFailure(Call<List<CafeteriaItem>> call, Throwable error) {
                if (!call.isCanceled()) {
                    menuStatus.setText(R.string.error_connection);
                }
            }
        });
    }

    private void renderCatalog(List<CafeteriaItem> products) {
        catalog.removeAllViews();
        purchaseButtons.clear();
        menuStatus.setText(products.isEmpty() ? R.string.shop_empty : R.string.shop_menu_hint);

        for (CafeteriaItem product : products) {
            View row = LayoutInflater.from(this).inflate(R.layout.item_cafeteria, catalog, false);

            TextView name = row.findViewById(R.id.tvItemName);
            TextView price = row.findViewById(R.id.tvItemPrice);
            TextView energy = row.findViewById(R.id.tvItemEnergy);
            Button buy = row.findViewById(R.id.btnBuyItem);

            name.setText(product.getName());
            price.setText(getString(R.string.shop_price, product.getPriceCoins()));
            energy.setText(getString(R.string.shop_recovery, product.getEnergyRecovery()));
            buy.setEnabled(profileLoaded && !purchasing);
            buy.setOnClickListener(v -> purchase(product.getIdItem()));

            purchaseButtons.add(buy);
            catalog.addView(row);
        }
    }

    private void purchase(String productId) {
        if (purchasing || !profileLoaded) {
            return;
        }

        String header = SessionStore.authorizationHeader(this);
        if (header == null) {
            SessionStore.signOut(this);
            return;
        }

        purchasing = true;
        setPurchaseEnabled(false);

        purchaseCall = RetrofitClient.getApiService()
                .buyCafeteriaItem(header, new CafeteriaOrder(productId));

        purchaseCall.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                purchasing = false;
                setPurchaseEnabled(profileLoaded);

                if (response.code() == 401) {
                    SessionStore.signOut(TiendaActivity.this);
                } else if (response.isSuccessful() && response.body() != null) {
                    StudentStats.render(stats, response.body());
                    profileStatus.setText("");
                    Toast.makeText(TiendaActivity.this,
                            R.string.shop_purchase_ok, Toast.LENGTH_SHORT).show();
                } else if (response.isSuccessful()) {
                    Toast.makeText(TiendaActivity.this,
                            R.string.error_empty_response, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(TiendaActivity.this,
                            ApiErrors.describe(TiendaActivity.this, response), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable error) {
                if (!call.isCanceled()) {
                    purchasing = false;
                    setPurchaseEnabled(profileLoaded);
                    Toast.makeText(TiendaActivity.this,
                            R.string.error_connection, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void setPurchaseEnabled(boolean enabled) {
        for (Button button : purchaseButtons) {
            button.setEnabled(enabled);
        }
    }

    @Override
    protected void onDestroy() {
        if (catalogCall != null) {
            catalogCall.cancel();
        }
        if (profileCall != null) {
            profileCall.cancel();
        }
        if (purchaseCall != null) {
            purchaseCall.cancel();
        }
        super.onDestroy();
    }
}