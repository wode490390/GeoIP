package cn.wode490390.nukkit.geoip.command;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.PluginIdentifiableCommand;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.lang.TranslationContainer;
import cn.nukkit.plugin.Plugin;
import cn.nukkit.utils.TextFormat;
import cn.wode490390.nukkit.geoip.GeoIP;

public class GeoIPCommand extends Command implements PluginIdentifiableCommand {

    private final Plugin plugin;

    public GeoIPCommand(Plugin plugin) {
        super("geoip", "Querys the GeoIP location of a player", "/geoip <player>");
        this.setPermission("geoip.show");
        this.getCommandParameters().clear();
        this.addCommandParameters("default", new CommandParameter[]{
                new CommandParameter("player", CommandParamType.TARGET, false)
        });
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!this.plugin.isEnabled() || !this.testPermission(sender)) {
            return false;
        }
        if (args.length > 0) {
            Player player = Server.getInstance().getPlayer(args[0]);
            if (player != null) {
                String geoLocation = GeoIP.query(player.getUniqueId());
                if (geoLocation != null) {
                    String[] ip = player.getAddress().split("\\.");
                    try {
                        sender.sendMessage(TextFormat.colorize("&6Player &c" + player.getDisplayName() + " &6comes from &c" + geoLocation + "&6. (IP:&c" + (sender.hasPermission("geoip.show.fullip") ? player.getAddress() : ip[0] + ".*.*." + ip[3]) + "&6)"));
                        return true;
                    } catch (Exception ex) {

                    }
                }
                sender.sendMessage(TextFormat.colorize("&6Player &c" + player.getDisplayName() + " &6comes from &aan unknown country&6."));
            } else {
                sender.sendMessage(new TranslationContainer("commands.generic.player.notFound"));
            }
        } else {
            sender.sendMessage(new TranslationContainer("commands.generic.usage", this.getUsage()));
        }
        return true;
    }

    @Override
    public Plugin getPlugin() {
        return this.plugin;
    }
}
