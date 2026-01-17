package datart.core.utils;

import java.util.regex.Pattern;

public final class SqlLimitUtils {

    // 匹配 /* ... */ 注释（支持多行）
    private static final Pattern BLOCK_COMMENT = Pattern.compile("/\\*.*?\\*/", Pattern.DOTALL);

    // 匹配 -- 注释 和 # 注释
    private static final Pattern LINE_COMMENT = Pattern.compile("(?m)(--|#).*?$");

    // 匹配字符串字面量：'...' 或 "..."
    private static final Pattern STRING_LITERAL = Pattern.compile("'([^'\\\\]|\\\\.)*'|\"([^\"\\\\]|\\\\.)*\"");

    // 匹配 LIMIT 关键字（词边界）
    private static final Pattern LIMIT_KEYWORD = Pattern.compile("(?i)\\blimit\\b");

    /**
     * 判断 SQL 是否包含 LIMIT 限制
     */
    public static boolean hasLimit(String sql) {
        if (sql == null || sql.isEmpty()) {
            return false;
        }

        // 1. 去掉块注释
        String cleaned = BLOCK_COMMENT.matcher(sql).replaceAll(" ");

        // 2. 去掉行注释
        cleaned = LINE_COMMENT.matcher(cleaned).replaceAll(" ");

        // 3. 去掉字符串字面量，防止 'limit'
        cleaned = STRING_LITERAL.matcher(cleaned).replaceAll(" ");

        // 4. 判断是否包含 limit 关键字
        return LIMIT_KEYWORD.matcher(cleaned).find();
    }

}
