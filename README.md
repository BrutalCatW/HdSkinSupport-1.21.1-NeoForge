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

## Сборка

```
$env:JAVA_HOME="C:\Program Files\Zulu\zulu-21"; ./gradlew.bat build
```

Результат — `build/libs/hdskinsupport-neoforge-1.0.1.jar`. Ставится **только в клиентские mods**,
серверу не нужен.

## Проверка

- `latest.log`: в списке модов `HD Skin Support 1.0.1 (hdskinsupport)`, строки `Skipping jar` нет;
- в игре HD-скин показывается, а не заменяется Стивом.
