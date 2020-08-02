package cn.wode490390.nukkit.geoip;

import cn.nukkit.Player;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;
import cn.wode490390.nukkit.geoip.command.GeoIPCommand;
import cn.wode490390.nukkit.geoip.util.LZMALib;
import cn.wode490390.nukkit.geoip.util.MetricsLite;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.maxmind.geoip2.DatabaseReader;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

public class GeoIP extends PluginBase {

    private static final Map<UUID, String> cache = Maps.newHashMap();

    /**
     * Querys player's geographical location.
     *
     * @param uuid
     * @return geographical location or null
     */
    public static String query(UUID uuid) {
        Preconditions.checkNotNull(uuid, "UUID cannot be null");
        return cache.get(uuid);
    }

    /**
     * Querys player's geographical location.
     *
     * @param player
     * @return geographical location or null
     *
     * @see #query(UUID) 
     */
    @Deprecated
    public static String query(Player player) {
        Preconditions.checkNotNull(player, "Player cannot be null");
        return query(player.getUniqueId());
    }

    static void setGeoLocation(UUID uuid, String location) {
        cache.put(uuid, location);
    }

    Config config;
    private File databaseFile;

    @Override
    public void onEnable() {
        try {
            new MetricsLite(this, 5375);
        } catch (Throwable ignore) {

        }

        this.saveDefaultConfig();
        this.config = this.getConfig();
        if (this.config.getBoolean("database.show-cities", false)) {
            this.databaseFile = new File(this.getDataFolder(), "GeoIP2-City.mmdb");
        } else {
            this.databaseFile = new File(this.getDataFolder(), "GeoIP2-Country.mmdb");
        }
        if (!this.databaseFile.exists()) {
            if (this.config.getBoolean("database.download-if-missing", true)) {
                this.downloadDatabase();
            } else {
                this.getLogger().warning("Can't find GeoIP database!");
                this.setEnabled(false);
                return;
            }
        } else if (this.config.getBoolean("database.update.enable", true)) {
            // try to update expired mmdb files
            long diff = new Date().getTime() - this.databaseFile.lastModified();
            if (diff / 86400000 > this.config.getLong("database.update.by-every-x-days", 30)) {
                this.downloadDatabase();
            }
        }
        DatabaseReader mmreader;
        try {
            // locale setting
            if (this.config.getBoolean("enable-locale")) {
                // If the locale is not avaliable, use "en".
                mmreader = new DatabaseReader.Builder(this.databaseFile).locales(Arrays.asList(this.config.getString("locale"), "en")).build();
            } else {
                mmreader = new DatabaseReader.Builder(this.databaseFile).build();
            }
        } catch (IOException ex) {
            this.getLogger().warning("Failed to read GeoIP database", ex);
            this.setEnabled(false);
            return;
        }
        this.getServer().getPluginManager().registerEvents(new GeoIPListener(this, mmreader), this);
        this.getServer().getCommandMap().register("geoip", new GeoIPCommand(this));
    }

    private void downloadDatabase() {
        try {
            String url;
            if (this.config.getBoolean("database.show-cities", false)) {
                url = this.config.getString("database.lzma-download-url-city", "https://cdn.jsdelivr.net/gh/wodeBot/geoipdb@lzma/city.mmdb.lzma");
            } else {
                url = this.config.getString("database.lzma-download-url", "https://cdn.jsdelivr.net/gh/wodeBot/geoipdb@lzma/country.mmdb.lzma");
            }
            if (Strings.isNullOrEmpty(url)) {
                this.getLogger().warning("GeoIP download url is empty.");
                return;
            }
            this.getLogger().info("Downloading GeoIP database... this might take a while.");
            URL downloadUrl = new URL(url);
            URLConnection conn = downloadUrl.openConnection();
            conn.setConnectTimeout(10000);
            conn.setRequestProperty("User-agent", "Mozilla/5.0 (iPad; CPU OS 13_6 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.1.2 Mobile/15E148 Safari/604.1");
            conn.connect();
            InputStream input = new BufferedInputStream(conn.getInputStream());
            OutputStream output = new FileOutputStream(this.databaseFile);
            LZMALib.decode(input, output);
            output.flush();
            output.close();
            input.close();
        } catch (MalformedURLException ex) {
            this.getLogger().warning("GeoIP download url is invalid", ex);
        } catch (IOException ex) {
            this.getLogger().warning("Failed to open connection", ex);
        }
    }
}
