# GeoIP
[![](https://i.loli.net/2019/08/11/g9PU5ufFoqmeKjp.png)](http://www.mcbbs.net/thread-900823-1-1.html "IP定位")

GeoIP plugin for Nukkit.

GeoIP provides an approximate lookup of where your players come from, based on their public IP and public geographical databases.

Please see [mcbbs](http://www.mcbbs.net/thread-900823-1-1.html) for more information.
## Permissions
| Command | Permission | Description | Default |
| - | - | - | - |
| `/geoip` | geoip.show | Shows the GeoIP location of a player. | OP |
| `/geoip` | geoip.show.fullip | Shows the full ip address of a player. | false |
| | geoip.hide | Allows player to hide player's country and city from people who have permission geoip.show | false |
## config.yml
```yaml
database:
  show-cities: false
  download-if-missing: true
  # Url for country
  download-url: "https://geolite.maxmind.com/download/geoip/database/GeoLite2-Country.tar.gz"
  # Url for cities
  download-url-city: "https://geolite.maxmind.com/download/geoip/database/GeoLite2-City.tar.gz"
  update:
    enable: true
    by-every-x-days: 30
show-on-login: true
# "enable-locale" enables locale on geolocation display.
enable-locale: true
# Not all languages are supported. See https://dev.maxmind.com/geoip/geoip2/web-services/#Languages
locale: en
```
## API Usage
```java
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.wode490390.nukkit.geoip.GeoIP;

class Example {
    Example() {
        Player player = Server.getInstance().getPlayer("wode490390");
        if (player != null) {
            String geoLocation = GeoIP.query(player); //Our API :)
            player.sendMessage("Your location: " + geoLocation);
        }
    }
}
```
