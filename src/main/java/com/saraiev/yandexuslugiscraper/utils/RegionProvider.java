package com.saraiev.yandexuslugiscraper.utils;

import lombok.Data;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

public class RegionProvider {

    public static Map<String, String> regionMap;

    static {
        regionMap = new HashMap<>();
        regionMap.put("Республика Адыгея", "11004-republic-of-adygea");
        regionMap.put("Республика Алтай", "10231-altai-republic");
        regionMap.put("Республика Башкортостан", "11111-republic-of-bashkortostan");
        regionMap.put("Республика Бурятия", "11330-republic-of-buryatia");
        regionMap.put("Республика Дагестан", "11010-republic-of-dagestan");
        regionMap.put("Республика Ингушетия", "11012-republic-of-ingushetia");
        regionMap.put("Кабардино-Балкарская Республика", "11013-the-kabardino-balkar-republic");
        regionMap.put("Республика Калмыкия", "11015-republic-of-kalmykia");
        regionMap.put("Карачаево-Черкесская Республика", "11020-karachay-cherkess-republic");
        regionMap.put("Республика Карелия", "10933-republic-of-karelia");
        regionMap.put("Республика Коми", "10939-komi-republic");
        regionMap.put("Республика Крым", "977-republic-of-crimea");
        regionMap.put("Республика Марий Эл", "11077-mari-el-republic");
        regionMap.put("Республика Мордовия", "11117-republic-of-mordovia");
        regionMap.put("Республика Саха (Якутия)", "11443-sakha-%28yakutia%29-republic");
        regionMap.put("Республика Северная Осетия", "11021-republic-of-north-ossetia-—-alania");
        regionMap.put("Республика Татарстан", "11119-republic-of-tatarstan");
        regionMap.put("Республика Тыва", "10233-tyva-republic");
        regionMap.put("Удмуртская Республика", "11148-udmurt-republic");
        regionMap.put("Республика Хакасия", "11340-republic-of-khakassia");
        regionMap.put("Чеченская Республика", "11024-chechen-republic");
        regionMap.put("Чувашская Республика", "11156-chuvash-republic");
        regionMap.put("Алтайский край", "11235-altai-krai");
        regionMap.put("Забайкальский край", "21949-zabaykalsky-krai");
        regionMap.put("Камчатский край", "11398-kamchatka-krai");
        regionMap.put("Краснодарский край", "10995-krasnodar-krai");
        regionMap.put("Красноярский край", "11309-krasnoyarsk-krai");
        regionMap.put("Пермский край", "11108-perm-krai");
        regionMap.put("Приморский край", "11409-primorsky-krai");
        regionMap.put("Ставропольский край", "11069-stavropol-krai");
        regionMap.put("Хабаровский край", "11457-khabarovsk-krai");
        regionMap.put("Амурская область", "11375-amur-oblast");
        regionMap.put("Архангельская область", "10842-arkhange%27lskaya-oblast%27");
        regionMap.put("Астраханская область", "10946-astrakhan-oblast");
        regionMap.put("Белгородская область", "10645-belgorod-oblast");
        regionMap.put("Брянская область", "10650-bryansk-oblast");
        regionMap.put("Владимирская область", "10658-vladimir-oblast");
        regionMap.put("Волгоградская область", "10950-volgograd-oblast");
        regionMap.put("Вологодская область", "10853-vologda-oblast");
        regionMap.put("Воронежская область", "10672-voronezh-oblast");
        regionMap.put("Ивановская область", "10687-ivanovo-oblast");
        regionMap.put("Иркутская область", "11266-irkutsk-oblast");
        regionMap.put("Калининградская область", "10857-kaliningrad-oblast");
        regionMap.put("Калужская область", "10693-kaluga-oblast");
        regionMap.put("Кемеровская область — Кузбасс", "11282-kemerovo-oblast");
        regionMap.put("Кировская область", "11070-kirov-oblast");
        regionMap.put("Костромская область", "10699-kostroma-oblast");
        regionMap.put("Курганская область", "11158-kurgan-oblast");
        regionMap.put("Курская область", "10705-kursk-oblast");
        regionMap.put("Ленинградская область", "10174-saint-petersburg-and-leningrad-oblast");
        regionMap.put("Липецкая область", "10712-lipetsk-oblast");
        regionMap.put("Магаданская область", "11403-magadan-district");
        regionMap.put("Московская область", "1-moscow-and-moscow-oblast");
        regionMap.put("Мурманская область", "10897-murmansk-oblast");
        regionMap.put("Нижегородская область", "11079-nizhny-novgorod-oblast%27");
        regionMap.put("Новгородская область", "10904-novgorod-oblast");
        regionMap.put("Новосибирская область", "11316-novosibirsk-oblast");
        regionMap.put("Омская область", "11318-omsk-oblast");
        regionMap.put("Оренбургская область", "11084-orenburg-oblast");
        regionMap.put("Орловская область", "10772-oryol-oblast");
        regionMap.put("Пензенская область", "11095-penza-oblast");
        regionMap.put("Псковская область", "10926-pskov-oblast");
        regionMap.put("Ростовская область", "11029-rostov-oblast");
        regionMap.put("Рязанская область", "10776-ryazan-oblast");
        regionMap.put("Самарская область", "11131-samara-oblast");
        regionMap.put("Саратовская область", "11146-saratov-oblast");
        regionMap.put("Сахалинская область", "11450-sakhalin-district");
        regionMap.put("Свердловская область", "11162-sverdlovsk-oblast");
        regionMap.put("Смоленская область", "10795-smolensk-oblast");
        regionMap.put("Тамбовская область", "10802-tambov-oblast");
        regionMap.put("Тверская область", "10819-tver-oblast");
        regionMap.put("Томская область", "11353-tomsk-district");
        regionMap.put("Тульская область", "10832-tula-oblast");
        regionMap.put("Тюменская область", "11176-tyumen-oblast");
        regionMap.put("Ульяновская область", "11153-ulyanovsk-oblast");
        regionMap.put("Челябинская область", "11225-chelyabinsk-oblast");
        regionMap.put("Ярославская область", "10841-yaroslavl-oblast");
        regionMap.put("Еврейская АО", "10243-jewish-autonomous-oblast");
        regionMap.put("Ненецкий АО", "10176-nenets-autonomous-okrug");
        regionMap.put("Ханты-Мансийский АО — Югра", "11193-khanty-mansi-autonomous-okrug-—-yugra");
        regionMap.put("Чукотский АО", "10251-chukotka-autonomous-okrug");
        regionMap.put("Ямало-Ненецкий АО", "11232-yamalo-nenets-autonomous-okrug");
    }

}
