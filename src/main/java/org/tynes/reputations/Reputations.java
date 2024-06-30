package org.tynes.reputations;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerJoinEvent;
import org.tynes.TyneLibF;

import java.sql.SQLException;

public class Reputations implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        try {
            if (!TyneLibF.getPlayerDatabase().playerExists(player)) {
                TyneLibF.getPlayerDatabase().addPlayer(player);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
