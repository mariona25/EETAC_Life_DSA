package edu.upc.dsa.eetaclife.ui;

import android.view.View;
import android.widget.TextView;

import edu.upc.dsa.eetaclife.R;
import edu.upc.dsa.eetaclife.models.User;

public final class StudentStats {

    private StudentStats() {}

    public static void render(View root, User user) {
        TextView ects = root.findViewById(R.id.tvStatsEcts);
        TextView energy = root.findViewById(R.id.tvStatsEnergy);
        TextView coins = root.findViewById(R.id.tvStatsCoins);

        ects.setText(root.getContext().getString(R.string.stats_ects_value, user.getEcts()));
        energy.setText(root.getContext().getString(R.string.stats_energy_value, user.getEnergy()));
        coins.setText(root.getContext().getString(R.string.stats_coins_value, user.getCoins()));

        root.setVisibility(View.VISIBLE);
    }
}