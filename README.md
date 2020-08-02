# GeoIP for Nukkit
[![Nukkit](https://img.shields.io/badge/Nukkit-1.0-green)](https://github.com/NukkitX/Nukkit)
[![Build](https://img.shields.io/circleci/build/github/wode490390/GeoIP/master)](https://circleci.com/gh/wode490390/GeoIP/tree/master)
[![Release](https://img.shields.io/github/v/release/wode490390/GeoIP)](https://github.com/wode490390/GeoIP/releases)
[![Release date](https://img.shields.io/github/release-date/wode490390/GeoIP)](https://github.com/wode490390/GeoIP/releases)
<!--[![MCBBS](https://img.shields.io/badge/-mcbbs-inactive)](https://www.mcbbs.net/thread-900823-1-1.html "IP定位")
[![Servers](https://img.shields.io/bstats/servers/5375)](https://bstats.org/plugin/bukkit/GeoIP/5375)
[![Players](https://img.shields.io/bstats/players/5375)](https://bstats.org/plugin/bukkit/GeoIP/5375)-->

GeoIP provides an approximate lookup of where your players come from, based on their public IP and public geographical databases.

If you found any bugs or have any suggestions, please open an issue on [GitHub Issues](https://github.com/wode490390/GeoIP/issues).

If you like this plugin, please star it on [GitHub](https://github.com/wode490390/GeoIP).

## Commands
| Command | Permission | Description | Default |
| - | - | - | - |
| `/geoip` | geoip.show | Shows the GeoIP location of a player. | OP |
| `/geoip` | geoip.show.fullip | Shows the full ip address of a player. | false |
| | geoip.hide | Allows player to hide player's country and city from people who have permission geoip.show | false |

## Configuration
<details>
<summary>config.yml</summary>

```yaml
database:
  show-cities: false
  download-if-missing: true
  # Url for country
  lzma-download-url: "https://cdn.jsdelivr.net/gh/wodeBot/geoipdb@lzma/country.mmdb.lzma"
  # Url for cities
  lzma-download-url-city: "https://cdn.jsdelivr.net/gh/wodeBot/geoipdb@lzma/city.mmdb.lzma"
show-on-login: true
# "enable-locale" enables locale on geolocation display.
enable-locale: true
# Not all languages are supported. See https://dev.maxmind.com/geoip/geoip2/web-services/#Languages
locale: en
```
</details>

## Download
- [Releases](https://github.com/wode490390/GeoIP/releases)
- [Snapshots](https://circleci.com/gh/wode490390/GeoIP)

## API Usage
<details>
<summary>example code</summary>

```java
import cn.wode490390.nukkit.geoip.GeoIP;
import java.util.UUID;

class Example {
    Example() {
        UUID uuid = UUID.fromString("ecb32467-6cee-4a59-b3c0-5468fec58ed4");
        String geoLocation = GeoIP.query(uuid); //Our API :)
        System.out.println("Location: " + geoLocation);
    }
}
```
</details>

## Compiling
1. Install [Maven](https://maven.apache.org/).
2. Fork and clone the repo.
3. Run `mvn clean package`. The compiled JAR can be found in the `target/` directory.

## Metrics Collection

This plugin uses [bStats](https://github.com/wode490390/bStats-Nukkit). You can opt out using the global bStats config; see the [official website](https://bstats.org/getting-started) for more details.

<!--[![Metrics](https://bstats.org/signatures/bukkit/GeoIP.svg)](https://bstats.org/plugin/bukkit/GeoIP/5375)-->

###### If I have any grammar and/or term errors, please correct them :)
