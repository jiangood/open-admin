package io.github.jiangood.openadmin.util;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * JDBC 结果取值工具。
 *
 * <p>MySQL 驱动对同一列可能返回 {@code Integer / Long / BigDecimal / Double / byte[]}，
 * 而调用方通常只关心「数字或默认值」。本类把各种取法显式命名，避免各业务再写私有 helper
 * 后出现「一处截断、一处四舍五入、一处直接返回 0」的口径分叉。</p>
 *
 * <p>典型用法：{@code JdbcRunner.findAll(sql)} 返回 {@code List<Map<String, Object>>}，
 * 取值时用 {@link #asLong(Object)}、{@link #str(Object)}、{@link #triBool(Object)} 等，
 * 不要直接强转。</p>
 */
public class JdbcValues {

    private JdbcValues() {
    }

    /**
     * 任意值 → 字符串，{@code null} 变空串（不 trim）。
     */
    public static String str(Object o) {
        return o == null ? "" : String.valueOf(o);
    }

    /**
     * 任意值 → 字符串并去掉首尾空白，{@code null} 变空串。
     */
    public static String strTrim(Object o) {
        return o == null ? "" : String.valueOf(o).trim();
    }

    /**
     * 任意值 → 字符串，{@code null} 保持 {@code null}（区分「没有这个字段」与「字段为空」）。
     */
    public static String strOrNull(Object o) {
        return o == null ? null : String.valueOf(o);
    }

    /**
     * 任意值 → {@code long}，失败返回 {@code 0}。
     *
     * <p>数字直接截断（{@code 3.7} → {@code 3}）；字符串先按整数解析，不是整数时按小数解析再截断，
     * 允许首尾空白。需要四舍五入请用 {@link #roundedLong(Object)}。</p>
     */
    public static long asLong(Object o) {
        if (o instanceof Number n) {
            return n.longValue();
        }
        if (o == null) {
            return 0L;
        }
        String s = String.valueOf(o).trim();
        try {
            return Long.parseLong(s);
        } catch (NumberFormatException ignored) {
            // 落到小数分支：MySQL DECIMAL / 模型侧 JSON 有时给 "3.7" 这种串
        }
        try {
            return (long) Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    /**
     * 任意值 → {@code long}，小数部分四舍五入，失败返回 {@code 0}。
     *
     * <p>统计口径专用（{@code "3.7"} → {@code 4}）；需要截断请用 {@link #asLong(Object)}。</p>
     */
    public static long roundedLong(Object o) {
        if (o instanceof Number n) {
            return n.longValue();
        }
        if (o == null) {
            return 0L;
        }
        try {
            return Math.round(Double.parseDouble(String.valueOf(o)));
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    /**
     * 任意值 → {@code int}，失败返回 {@code def}。
     */
    public static int asInt(Object o, int def) {
        if (o instanceof Number n) {
            return n.intValue();
        }
        if (o == null) {
            return def;
        }
        try {
            return Integer.parseInt(String.valueOf(o).trim());
        } catch (NumberFormatException e) {
            return def;
        }
    }

    /**
     * 任意值 → {@code double}，失败返回 {@code def}。
     */
    public static double asDouble(Object o, double def) {
        if (o instanceof Number n) {
            return n.doubleValue();
        }
        if (o == null) {
            return def;
        }
        try {
            return Double.parseDouble(String.valueOf(o));
        } catch (NumberFormatException e) {
            return def;
        }
    }

    /**
     * 任意值 → {@code double}，失败返回 {@link Double#NaN}。
     */
    public static double asDoubleNaN(Object o) {
        return asDouble(o, Double.NaN);
    }

    /**
     * 任意值 → 布尔，两态（识别不了按 {@code false}）。三态请用 {@link #triBool(Object)}。
     */
    public static boolean bool(Object o) {
        Boolean value = triBool(o);
        return value != null && value;
    }

    /**
     * 三态布尔（{@code null} = 未知）：不要把「还没抓过」错画成「离线」。
     */
    public static Boolean triBool(Object o) {
        if (o == null) {
            return null;
        }
        if (o instanceof Boolean b) {
            return b;
        }
        if (o instanceof Number n) {
            return n.intValue() != 0;
        }
        String s = str(o).trim().toLowerCase();
        if (s.isEmpty()) {
            return null;
        }
        if ("1".equals(s) || "true".equals(s)) {
            return true;
        }
        if ("0".equals(s) || "false".equals(s)) {
            return false;
        }
        return null;
    }

    /**
     * 库里的时间列 → {@link LocalDateTime}；识别不了返回 {@code null}。
     */
    public static LocalDateTime localDateTime(Object o) {
        if (o == null) {
            return null;
        }
        if (o instanceof LocalDateTime ldt) {
            return ldt;
        }
        if (o instanceof Timestamp ts) {
            return ts.toLocalDateTime();
        }
        if (o instanceof Date date) {
            return new Timestamp(date.getTime()).toLocalDateTime();
        }
        return null;
    }

    /**
     * 库里的时间列 → epoch 秒（前端统一按秒渲染「多久之前」）；识别不了返回 {@code null}。
     */
    public static Long epochSec(Object o) {
        if (o == null) {
            return null;
        }
        try {
            if (o instanceof Instant instant) {
                return instant.getEpochSecond();
            }
            if (o instanceof Timestamp ts) {
                return ts.toInstant().getEpochSecond();
            }
            if (o instanceof LocalDateTime ldt) {
                return ldt.atZone(ZoneId.systemDefault()).toEpochSecond();
            }
            if (o instanceof Date d) {
                return d.getTime() / 1000;
            }
            if (o instanceof Number n) {
                return n.longValue();
            }
            String s = str(o).trim();
            return s.isEmpty() ? null : LocalDateTime.parse(s.replace(' ', 'T'))
                    .atZone(ZoneId.systemDefault()).toEpochSecond();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 任意值 → {@link Long}，失败返回 {@code null}（HTTP 入参解析用，避免把「没传」读成 0）。
     */
    public static Long longOrNull(Object o) {
        if (o instanceof Number n) {
            return n.longValue();
        }
        if (o == null) {
            return null;
        }
        try {
            return Long.parseLong(String.valueOf(o).trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 任意值 → {@link Integer}，失败返回 {@code null}（HTTP 入参解析用）。
     */
    public static Integer intOrNull(Object o) {
        if (o instanceof Number n) {
            return n.intValue();
        }
        if (o == null) {
            return null;
        }
        try {
            return Integer.parseInt(String.valueOf(o).trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
