package me.mika.midomikasuperhighway;

import me.mika.midomikasuperhighway.Listeners.WalkOnHighWay;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;

public final class MidoMika_SuperHighWay extends JavaPlugin {
    private static MidoMika_SuperHighWay plugin;
    public static MidoMika_SuperHighWay getPlugin() {

        return plugin;

    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        plugin = this;
        getServer().getPluginManager().registerEvents(new WalkOnHighWay(),this);

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
