package cn.wode490390.nukkit.geoip;

import cn.nukkit.Player;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.ice.tar.TarEntry;
import com.ice.tar.TarInputStream;
import com.maxmind.geoip2.DatabaseReader;
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
import java.util.zip.GZIPInputStream;

public class GeoIP extends PluginBase {

    private static final Map<Player, String> cache = Maps.newHashMap();

    /**
     * Querys player's geographical location.
     *
     * @param palyer
     * @return geographical location or null
     */
    public static String query(Player palyer) {
        Preconditions.checkNotNull(palyer, "Player cannot be null");
        return cache.get(palyer);
    }

    static void setGeoLocation(Player palyer, String location) {
        cache.put(palyer, location);
    }

    Config config;
    private File databaseFile;

    @Override
    public void onEnable() {
        try {
            new MetricsLite(this);
        } catch (Exception ignore) {

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
                url = this.config.getString("database.download-url-city");
            } else {
                url = this.config.getString("database.download-url");
            }
            if (url == null || url.isEmpty()) {
                this.getLogger().warning("GeoIP download url is empty.");
                return;
            }
            this.getLogger().info("Downloading GeoIP database... this might take a while.");
            URL downloadUrl = new URL(url);
            URLConnection conn = downloadUrl.openConnection();
            conn.setConnectTimeout(10000);
            conn.connect();
            InputStream input = conn.getInputStream();
            OutputStream output = new FileOutputStream(this.databaseFile);
            byte[] buffer = new byte[2048];
            if (url.endsWith(".gz")) {
                input = new GZIPInputStream(input);
                if (url.endsWith(".tar.gz")) {
                    // The new GeoIP2 uses tar.gz to pack the db file along with some other txt. So it makes things a bit complicated here.
                    String filename;
                    TarInputStream tarInputStream = new TarInputStream(input);
                    TarEntry entry;
                    while ((entry = tarInputStream.getNextEntry()) != null) {
                        if (!entry.isDirectory()) {
                            filename = entry.getName();
                            if (filename.substring(filename.length() - 5).equalsIgnoreCase(".mmdb")) {
                                input = tarInputStream;
                                break;
                            }
                        }
                    }
                }
            }
            int length = input.read(buffer);
            while (length >= 0) {
                output.write(buffer, 0, length);
                length = input.read(buffer);
            }
            output.close();
            input.close();
        } catch (MalformedURLException ex) {
            this.getLogger().warning("GeoIP download url is invalid", ex);
        } catch (IOException ex) {
            this.getLogger().warning("Failed to open connection", ex);
        }
    }
}
