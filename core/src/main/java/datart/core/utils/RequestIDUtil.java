package datart.core.utils;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Random;
import lombok.extern.slf4j.Slf4j;

/**
 * 请求 ID 工具类.
 *
 * @author suxinshuo
 * @date 2024/6/26 15:07
 */
@Slf4j
public class RequestIDUtil {

    private static final Random RANDOM = new Random();

    private static final ReqIdGenerator REQ_ID_GENERATOR;
    private static final ThreadLocal<String> CURRENT_REQUEST_ID;
    private static final String REQUEST_ID_DEFAULT;

    static {
        REQ_ID_GENERATOR = new ReqIdLegacyGenerator(RANDOM);
        CURRENT_REQUEST_ID = new ThreadLocal<>();
        REQUEST_ID_DEFAULT = "00000000000000000000000000000000";
    }

    private RequestIDUtil() {
    }

    public static String getCurrentRequestID() {
        return CURRENT_REQUEST_ID.get();
    }

    public static void setCurrentRequestID(String requestID) {
        CURRENT_REQUEST_ID.set(requestID);
    }

    public static void removeCurrentRequestID() {
        CURRENT_REQUEST_ID.remove();
    }

    public static String generateRequestID() {
        try {
            return REQ_ID_GENERATOR.generate();
        } catch (Exception var1) {
            log.error("Failed to generate request id, use {} instead.", REQUEST_ID_DEFAULT);
            return REQUEST_ID_DEFAULT;
        }
    }

    static class ReqIdLegacyGenerator implements ReqIdGenerator {

        private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
        private final Random random;
        private final String localIp;

        public ReqIdLegacyGenerator(Random random) {
            this.random = random;
            this.localIp = formatIpAddress();
        }

        @Override
        public String generate() {
            LocalDateTime now = LocalDateTime.now();
            return DATETIME_FORMATTER.format(now) + this.localIp + RequestIDUtil.ReqIdGenerator.randomHexString(this.random, 3, true);
        }

        static String formatIpAddress() {
            Inet4Address inet4Address = getLocalIpv4();
            if (Objects.isNull(inet4Address)) {
                return "000000000000";
            }
            byte[] address = inet4Address.getAddress();
            return String.format("%03d%03d%03d%03d", address[0] & 255, address[1] & 255, address[2] & 255, address[3] & 255);
        }

        private static Inet4Address getLocalIpv4() {
            try {
                InetAddress localHost = InetAddress.getLocalHost();
                if (localHost instanceof Inet4Address) {
                    return (Inet4Address) localHost;
                }
            } catch (Exception e) {
                log.warn("Get local ip address error.", e);
            }
            log.warn("Failed to get local ip, use 0.0.0.0 instead.");
            try {
                return (Inet4Address) InetAddress.getByName("0.0.0.0");
            } catch (UnknownHostException var3) {
                log.warn("Failed to get 0.0.0.0 address., e");
            }
            return null;
        }
    }

    interface ReqIdGenerator {

        char[] DIGITS = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

        String generate();

        static String randomHexString(Random random, int length, boolean upperCase) {
            char[] out = new char[length];
            int i = 0;

            while (i < length) {
                int rnd = random.nextInt();

                for (int n = Math.min(length - i, 8); n-- > 0; rnd >>= 4) {
                    out[i++] = DIGITS[rnd & 15];
                }
            }

            return upperCase ? (new String(out)).toUpperCase() : new String(out);
        }
    }

}
