# HD Skin Support — NeoForge 1.21.1

Клиентский мод: скины крупнее 64×64 (128×128, 256×256 и т.д.) больше не отбрасываются игрой.
Авторы — dima_dencep, Gravita (GravitLauncher), лицензия MIT. Здесь — перенос на NeoForge 1.21.1
для будущего сервера проекта BrutalCat.

## Почему перенос

Исходный `hdskinsupport-neoforge.jar` (лежит рядом, в `MODS\PROJECT_MODS\`) собран под NeoForge
1.20.2–1.20.4. На NeoForge 21.1.252 (FML 4.0.44) клиент его пропускает:

```
Skipping jar. File mods\hdskinsupport-neoforge.jar is for Minecraft Forge or an older version of NeoForge, and cannot be loaded
```

- описание мода было в `META-INF/mods.toml`, FML 4 читает только `META-INF/neoforge.mods.toml`;
  в нём же версия Minecraft ограничена `[1.20.2,1.21)`;
- миксин подключался строкой `MixinConfigs` в MANIFEST — FML 4 берёт конфиги миксинов только
  из `[[mixins]]` в `neoforge.mods.toml`.

Сам код миксина под 1.21.1 годен (сверено `javap` по клиентскому jar 1.21.1): `HttpTexture.processLegacySkin`,
`setNoAlpha`, `doNotchTransparencyHack` и методы `NativeImage` — с теми же сигнатурами.
Ходившая в обсуждениях замена `PlayerSkinTexture` → `PlayerSkinTextureDownloader` — это имена Fabric
для более новых версий Minecraft, к 1.21.1 не относится.

Что изменено против исходника: новое описание мода (`lowcodefml`, `[[mixins]]`, Minecraft
`[1.21.1,1.21.2)`); приватные методы `HttpTexture` — через `@Shadow` вместо access transformer;
логгер свой; шаблонный класс мода (API NeoForge 1.20, которого в 21 нет) убран; `pack_format` 34.
Логика обработки скина — без изменений.

## Совместимость с Entity Texture Features (1.0.3)

ETF 7.2.5 меняет тот же метод `HttpTexture.processLegacySkin`: открывает его своим access transformer и встраивается
внутрь. С заменой метода целиком (`@Overwrite`) клиент падал дважды:

- 1.0.1 (`@Overwrite private`) — замена закрытее цели, открытой ETF:
  ```
  Mixin apply for mod hdskinsupport failed hdskinsupport.mixins.json:MagicMixin -> net.minecraft.client.renderer.texture.HttpTexture:
  PRIVATE @Overwrite method processLegacySkin ... cannot reduce visibiliy of PUBLIC target method
  ```
- 1.0.2 (`@Overwrite public`) — в целиком заменённый метод Mixin 0.8.7 пускает чужую вставку, только если приоритет
  заменившего миксина ниже; у обоих 1000, и падал уже миксин ETF:
  ```
  Mixin [entity_texture_features.mixins.json:MixinPlayerSkinTexture ...] FAILED during APPLY
  ... etf$differentAlpha with priority 1000 cannot inject into HttpTexture::processLegacySkin ... merged by ...MagicMixin with priority 1000
  ```

С 1.0.3 метод не заменяется: вставка в начало (`@Inject` в `HEAD`, такие Mixin разрешает всегда). Скин шириной 64
(64×64, 64×32) идёт ванильным кодом — ETF и другие моды встраиваются в него как обычно; более крупный обрабатывается
кодом мода (логика авторов без изменений) и сразу возвращается. Обработка ETF внутри этого метода на HD-скины не действует.

## Сборка

```
$env:JAVA_HOME="C:\Program Files\Zulu\zulu-21"; ./gradlew.bat build
```

Результат — `build/libs/HdSkinSupport-NeoForge-1.0.3.jar` (до 15.10.2026 — `hdskinsupport-neoforge-1.0.3.jar`; старый jar из mods
убрать). Ставится **только в клиентские mods**,
серверу не нужен.

## Проверка

- `latest.log`: в списке модов `HD Skin Support 1.0.3 (hdskinsupport)`, строки `Skipping jar` нет;
- в игре HD-скин показывается, а не заменяется Стивом.
