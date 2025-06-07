package io.github.mcengine.addon.currency.entity.listener;

import io.github.mcengine.api.currency.MCEngineCurrencyApi;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.plugin.Plugin;

import java.util.Random;

public class EntityListener implements Listener {

    private final Plugin plugin;
    private final MCEngineCurrencyApi currencyApi;
    private final Random random;

    public EntityListener(Plugin plugin) {
        this.plugin = plugin;
        this.currencyApi = MCEngineCurrencyApi.getApi();
        this.random = new Random();
    }

    @EventHandler
    public void onEntityKill(EntityDeathEvent event) {
        if (event.getEntityType() != EntityType.ZOMBIE) return;

        Player killer = event.getEntity().getKiller();
        if (killer == null) return;

        int reward = 100 + random.nextInt(101); // 100-200
        currencyApi.addCoin(killer.getUniqueId(), "coin", reward);

        killer.sendMessage("§aYou received §e" + reward + " §acoins for killing a zombie!");
    }
}
