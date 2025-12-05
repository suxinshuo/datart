package datart.core.common;

import cn.hutool.crypto.Mode;
import cn.hutool.crypto.Padding;
import cn.hutool.crypto.symmetric.AES;

/**
 * 安全相关工具方法
 *
 * @author suxinshuo
 * @date 2025/12/5 20:17
 */
public class SecureAesUtils {

    /**
     * 16 字符 = 128 位密钥
     */
    private static final String KEY = "1234567890123456";

    /**
     * 16 字符 = 128 位 IV(CBC 模式必需)
     */
    private static final String IV = "abcdefghijklmnop";

    private static final AES AES_INSTANCE = new AES(
            Mode.CBC,
            Padding.PKCS5Padding,
            KEY.getBytes(),
            IV.getBytes()
    );

    public static String encrypt(String content) {
        return AES_INSTANCE.encryptBase64(content);
    }

    public static String decrypt(String content) {
        return AES_INSTANCE.decryptStr(content);
    }

}
