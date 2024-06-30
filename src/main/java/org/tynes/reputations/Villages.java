package org.tynes.reputations;

import cn.nukkit.Player;
import cn.nukkit.entity.passive.EntityVillager;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDeathEvent;
import cn.nukkit.level.Sound;
import org.tynes.TyneLibF;
import org.tynes.database.PlayerDatabase;

import java.sql.SQLException;
import java.util.Random;

public class Villages implements Listener {

    @EventHandler
    public void onVillagerDeath(EntityDeathEvent e) {
        if (e.getEntity().getNetworkId() == EntityVillager.NETWORK_ID) {  //if Entity is Villager

            if (e.getEntity().getLastDamageCause() instanceof EntityDamageByEntityEvent) { //if Villager died because of Entity

                EntityDamageByEntityEvent event = (EntityDamageByEntityEvent) e.getEntity().getLastDamageCause();
                if (event.getDamager() instanceof Player) {  //if Damager is player

                    Player player = (Player) event.getDamager();
                    PlayerDatabase pd = TyneLibF.getPlayerDatabase();

                    Random rnd = new Random();
                    int value = rnd.nextInt(15 - 7) + 7;

                    try {
                        pd.removeReputations(player, value);
                        player.sendTitle("§r ");
                        player.setSubtitle("§c-" + value + " Reputations");
                        player.level.addSound(player.getDirectionVector(), Sound.MOB_ENDERDRAGON_HIT, 1F, 0.5F);
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }

                }

            }
        }
    }
}
