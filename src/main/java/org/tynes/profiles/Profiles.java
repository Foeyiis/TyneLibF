package org.tynes.profiles;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerJoinEvent;
import cn.nukkit.level.Sound;
import cn.nukkit.utils.TextFormat;
import com.formconstructor.form.CustomForm;
import com.formconstructor.form.SimpleForm;
import com.formconstructor.form.element.SelectableElement;
import com.formconstructor.form.element.custom.Dropdown;
import com.formconstructor.form.element.custom.Input;
import com.formconstructor.form.element.simple.Button;
import com.formconstructor.form.element.simple.ImageType;
import org.tynes.CustomConfig.Message;
import org.tynes.TyneLibF;
import org.tynes.database.PlayerDatabase;

import java.sql.SQLException;

public class Profiles implements Listener {

    private static SimpleForm noprofileform;
    private static CustomForm createform;
    private static PlayerDatabase pd = TyneLibF.getPlayerDatabase();

    public static SimpleForm noProfileForm() {
        noprofileform = new SimpleForm("Profiles");
        noprofileform.addContent("You don't have a profile");
        noprofileform.addButton(new Button("Create a new profile")
                .setImage(ImageType.URL, "https://cdn-icons-png.freepik.com/128/11607/11607148.png")
                .onClick(((player, button) -> {
                    createform.send(player);
                }))
        );
        noprofileform.setNoneHandler(player -> {
            player.sendMessage(TextFormat.colorize("&cYou have to create a new profile!"));
            player.level.addSound(player.getLocation(), Sound.NOTE_BASS);
            TyneLibF.getInstance().getServer().getScheduler().scheduleDelayedTask(TyneLibF.getInstance(), () -> {
                noprofileform.send(player);
            }, 20);
            return;
        });
        return noprofileform;
    }

    public static CustomForm createForm(Player player) {
        createform = new CustomForm("Create Profiles");
        try {
            String nameValue = "Profile-" + pd.getProfilesNumber(player) + 1;
            createform.addElement("ProfileName", new Input("ProfileName")
                    .setPlaceholder("Profile-" + nameValue)
                    .setDefaultValue("Profile-" + nameValue)
            );
            createform.addElement("ProfileMode", new Dropdown("ProfileMode")
                    .addElement("Craftman")
                    .addElement("Ironman")
                    .addElement("Normal")
                    .addElement("Hardcore")
            );
        } catch (SQLException e) {
            e.printStackTrace();
        }

        createform.addElement(
                "Attention! Your items from your previous profile will not appear on your new profile or be different from the one you are using now!\n"
        );

        createform.addElement(
                Message.hardcoreDesc
        );

        createform.addElement(
                Message.normalDesc
        );

        createform.addElement(
                Message.ironmanDesc
        );

        createform.addElement(
                Message.craftmanDesc
        );

        createform.setHandler(((player1, response) -> {
            String profileNames = response.getInput("ProfileName").getValue();
            SelectableElement profileModes = response.getDropdown("ProfileMode").getValue();
        }));

        createform.setNoneHandler((player1 -> {
            try {
                if (pd.getProfilesNumber(player1) <= 0) {
                    noprofileform.send(player);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }));

        return createform;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PlayerDatabase pd = TyneLibF.getPlayerDatabase();

        if (player.getFirstPlayed() == null) {

        }
    }

}
