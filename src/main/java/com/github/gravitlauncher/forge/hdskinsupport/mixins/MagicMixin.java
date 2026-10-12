package com.github.gravitlauncher.forge.hdskinsupport.mixins;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.HttpTexture;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * HD-скины. Ванильный {@code HttpTexture.processLegacySkin} пропускает только 64×32 и 64×64, остальное
 * выбрасывает. Здесь те же операции (дорисовка старого формата 64×32, сброс прозрачности), но в масштабе
 * «ширина / 64» — скины 128×128, 256×256 и т.д. проходят.
 *
 * Код авторов (dima_dencep, Gravita; MIT), логика без изменений. Перенос на NeoForge 1.21.1:
 * приватные статические методы HttpTexture — через {@link Shadow} вместо access transformer, логгер свой
 * (шаблонный класс мода с API NeoForge 1.20 убран). С 1.0.3 скины шириной 64 оставлены ванильному коду —
 * вставка в начало метода вместо {@code @Overwrite} (совместимость с Entity Texture Features).
 */
@Mixin(HttpTexture.class)
public class MagicMixin {

    @Shadow
    private static void setNoAlpha(NativeImage image, int x, int y, int width, int height) {
        throw new AssertionError();
    }

    @Shadow
    private static void doNotchTransparencyHack(NativeImage image, int x, int y, int width, int height) {
        throw new AssertionError();
    }

    /**
     * Скин шириной 64 (64×64, 64×32) — ванильный код, в него встраиваются и другие моды (Entity Texture Features);
     * крупнее — обработка здесь. Вставка в начало, а не {@code @Overwrite}: в целиком заменённый метод Mixin не пускает
     * чужие вставки с тем же приоритетом, и ETF ронял клиент.
     */
    @Inject(method = "processLegacySkin", at = @At("HEAD"), cancellable = true)
    private void hdskinsupport$processHdSkin(NativeImage image, CallbackInfoReturnable<NativeImage> cir) {
        if (image.getWidth() != 64) {
            cir.setReturnValue(hdskinsupport$processLegacySkin(image));
        }
    }

    /**
     * Код авторов (dima_dencep, Gravita) без изменений: ванильная обработка в масштабе «ширина / 64».
     */
    @Unique
    private NativeImage hdskinsupport$processLegacySkin(NativeImage image) {
        int height = image.getHeight();
        int width = image.getWidth();
        int scale = width / 8;
        int scaleX64 = scale * 8;
        int scaleX32 = scale * 4;
        int scaleX16 = scale * 2;
        int scaleX4 = scale / 2;
        int scaleX12 = scale + scaleX4;
        int scaleX20 = scaleX16 + scaleX4;
        int scaleX24 = scaleX20 + scaleX4;
        int scaleX48 = scale * 6;
        int scaleX44 = scaleX48 - scaleX4;
        int scaleX40 = scale * 5;
        int scaleX52 = scaleX48 + scaleX4;
        if (width == scaleX64 && (height == scaleX32 || height == scaleX64)) {
            boolean legacy = height == scaleX32;
            if (legacy) {
                NativeImage newImage = new NativeImage(scaleX64, scaleX64, true);
                newImage.copyFrom(image);
                image.close();
                image = newImage;
                newImage.fillRect(0, scaleX32, scaleX64, scaleX32, 0);
                newImage.copyRect(scaleX4, scaleX16, scaleX16, scaleX32, scaleX4, scaleX4, true, false);
                newImage.copyRect(scale, scaleX16, scaleX16, scaleX32, scaleX4, scaleX4, true, false);
                newImage.copyRect(0, scaleX20, scaleX24, scaleX32, scaleX4, scaleX12, true, false);
                newImage.copyRect(scaleX4, scaleX20, scaleX16, scaleX32, scaleX4, scaleX12, true, false);
                newImage.copyRect(scale, scaleX20, scale, scaleX32, scaleX4, scaleX12, true, false);
                newImage.copyRect(scaleX12, scaleX20, scaleX16, scaleX32, scaleX4, scaleX12, true, false);
                newImage.copyRect(scaleX44, scaleX16, -scale, scaleX32, scaleX4, scaleX4, true, false);
                newImage.copyRect(scaleX48, scaleX16, -scale, scaleX32, scaleX4, scaleX4, true, false);
                newImage.copyRect(scaleX40, scaleX20, 0, scaleX32, scaleX4, scaleX12, true, false);
                newImage.copyRect(scaleX44, scaleX20, -scale, scaleX32, scaleX4, scaleX12, true, false);
                newImage.copyRect(scaleX48, scaleX20, -scaleX16, scaleX32, scaleX4, scaleX12, true, false);
                newImage.copyRect(scaleX52, scaleX20, -scale, scaleX32, scaleX4, scaleX12, true, false);
            }

            setNoAlpha(image, 0, 0, scaleX32, scaleX16);
            if (legacy) {
                doNotchTransparencyHack(image, scaleX32, 0, scaleX64, scaleX32);
            }

            setNoAlpha(image, 0, scaleX16, scaleX64, scaleX32);
            setNoAlpha(image, scaleX16, scaleX48, scaleX48, scaleX64);
            return image;
        } else {
            image.close();
            LoggerFactory.getLogger("hdskinsupport").warn("Skip incorrectly sized ({}x{}) skin texture", width, height);
            return null;
        }
    }
}
