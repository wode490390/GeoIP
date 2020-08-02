package cn.wode490390.nukkit.geoip;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerJoinEvent;
import cn.nukkit.scheduler.NukkitRunnable;
import cn.nukkit.utils.TextFormat;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.AddressNotFoundException;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.model.CountryResponse;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;

public class GeoIPListener implements Listener {

    private final GeoIP plugin;
    private final DatabaseReader mmreader;

    GeoIPListener(GeoIP plugin, DatabaseReader mmreader) {
        this.plugin = plugin;
        this.mmreader = mmreader;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        new NukkitRunnable() {
            @Override
            public void run() {
                try {
                    delayedJoin(event.getPlayer());
                } catch (UnknownHostException ex) {
                    plugin.getLogger().error("Invalid ip address", ex);
                }
            }
        }.runTaskAsynchronously(this.plugin);
    }

    private void delayedJoin(Player player) throws UnknownHostException {
        if (player.hasPermission("geoip.hide")) {
            return;
        }
        InetAddress address = InetAddress.getByName(player.getAddress());
        StringBuilder sb = new StringBuilder();
        try {
            if (this.plugin.config.getBoolean("database.show-cities", false)) {
                CityResponse response = this.mmreader.city(address);
                if (response == null) {
                    return;
                }
                String city;
                String region;
                String country;
                city = response.getCity().getName();
                region = response.getMostSpecificSubdivision().getName();
                country = response.getCountry().getName();
                if (city != null) {
                    sb.append(city).append(", ");
                }
                if (region != null) {
                    sb.append(region).append(", ");
                }
                sb.append(country);
            } else {
                CountryResponse response = this.mmreader.country(address);
                sb.append(response.getCountry().getName());
            }
        } catch (AddressNotFoundException ex) {
            String msg = TextFormat.colorize("&6Player &c" + player.getDisplayName() + " &6comes from &aan unknown country&6.");
            if (checkIfLocal(address)) {
                this.plugin.getServer().getOnlinePlayers().values().stream()
                        .filter(p -> p.hasPermission("geoip.show"))
                        .forEach(p -> p.sendMessage(msg));
                return;
            }
            this.plugin.getLogger().info("Unknown ip: " + player.getAddress() + " (" + player.getName() + ")");
        } catch (IOException | GeoIp2Exception ex) {
            // GeoIP2 API forced this when address not found in their DB. jar will not complied without this.
            this.plugin.getLogger().warning("Failed to read GeoIP database: " + ex.getLocalizedMessage());
        }
        GeoIP.setGeoLocation(player.getUniqueId(), sb.toString());
        if (this.plugin.config.getBoolean("show-on-login", true)) {
            String template = TextFormat.colorize("&6Player &c" + player.getDisplayName() + " &6comes from &c" + sb.toString() + "&6. (IP:&c%ip%&6)");
            String[] ip = player.getAddress().split("\\.");
            String anonym;
            try {
                anonym = ip[0] + ".*.*." + ip[3];
            } catch (Exception ex) {
                throw new UnknownHostException(ex.getLocalizedMessage());
            }
            String msg = template.replace("%ip%", player.getAddress());
            String anonymousMsg = template.replace("%ip%", anonym);
            player.getServer().getOnlinePlayers().values().stream()
                    .filter(p -> p.hasPermission("geoip.show"))
                    .forEach(p -> p.sendMessage(p.hasPermission("geoip.show.fullip") ? msg : anonymousMsg));
        }
    }

    private boolean checkIfLocal(InetAddress address) {
        if (address.isAnyLocalAddress() || address.isLoopbackAddress()) {
            return true;
        }
        // Double checks if address is defined on any interface
        try {
            return NetworkInterface.getByInetAddress(address) != null;
        } catch (SocketException e) {
            return false;
        }
    }
}
