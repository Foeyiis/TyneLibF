package org.tynes;

import cn.nukkit.plugin.PluginBase;
import cn.nukkit.plugin.PluginManager;
import org.tynes.database.PlayerDatabase;
import org.tynes.profiles.Profiles;
import org.tynes.reputations.Reputations;
import org.tynes.reputations.Villages;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

public class TyneLibF extends PluginBase {

    private static TyneLibF tyneLibF;
    private static PlayerDatabase playerDatabase;

    @Override
    public void onEnable() {
        // Called when plugin is enabled. This is called after all plugins have been LOADED
        // If your plugin has public API that could be accessed by other plugins, you will want to
        // make sure to initialize that in onLoad instead of here

        tyneLibF = this;
        double loadTimes = System.currentTimeMillis();

        saveDefaultConfig();

        File db = new File(getDataFolder(), "player.db");
        File profileDb = new File(getDataFolder() + "\\database", "\\profiles.db");

        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }
        if (!(new File(getDataFolder() + "\\database")).exists()) {
            new File(getDataFolder() + "\\database").mkdirs();
        }

        try {
            boolean db_success = db.exists() ? true :
                    db.createNewFile();
            boolean profileDB_success = profileDb.exists() ? true :
                    profileDb.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (getConfig().get("database-connection").toString().equalsIgnoreCase("sqlite")) {
            try {
                playerDatabase = new PlayerDatabase(db.getAbsolutePath(), profileDb.getAbsolutePath());
            } catch (SQLException e) {
                e.printStackTrace();
                getLogger().error("Failed to connect to database! " + e.getMessage());
                getServer().getPluginManager().disablePlugin(this);
            }
        }

        PluginManager pm = getServer().getPluginManager();

        pm.registerEvents(new Villages(), this);
        pm.registerEvents(new Reputations(), this);

        pm.registerEvents(new Profiles(), this);

        loadTimes = System.currentTimeMillis() - loadTimes;
        getLogger().info("§aPlugin loaded within " + loadTimes + "ms");
    }

    @Override
    public void onDisable() {
        // Called when plugin is disabled. This would be done by the server when it shuts down
        // so this is a good idea to save any persistant data you need.
        // May also be called if an exception is called during loading/enabling of your plugin

        try {
            playerDatabase.closeConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        tyneLibF = null;
    }

    public static TyneLibF getInstance() {
        return tyneLibF;
    }

    public static PlayerDatabase getPlayerDatabase() {
        return playerDatabase;
    }

}