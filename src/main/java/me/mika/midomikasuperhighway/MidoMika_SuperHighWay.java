package me.mika.midomikasuperhighway;

import me.mika.midomikasuperhighway.Listeners.WalkOnHighWay;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;

public final class MidoMika_SuperHighWay extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        getServer().getPluginManager().registerEvents(new WalkOnHighWay(),this);

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
