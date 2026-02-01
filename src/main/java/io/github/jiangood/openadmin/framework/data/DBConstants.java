package io.github.jiangood.openadmin.framework.data;


public class DBConstants {


    // 用于存储大量二进制数据，如图片、音频、视频等
    public static final String TYPE_BLOB = "blob";

    public static final int LEN_ID = 32;
    public static final int LEN_NAME = 50;
    public static final int LEN_PASSWORD = 32;
    public static final int LEN_DATE = 10; // 2010-01-01

    public static final int LEN_PHONE = 14; // +86
    public static final int LEN_TEL = 14; // 0851-8524990
    public static final int LEN_EMAIL = 50;

    public static final int LEN_MAX_VARCHAR = 16383;
    public static final int LEN_TEXT = 65536;

    /**
     * 金额精度，小数点后的位数
     */
    public static final int MONEY_SCALE = 2;

    /**
     * 金额精度，小数点前的位数
     */
    public static final int MONEY_PRECISION = 12; //
    public static final int LEN_CODE = 20;

    /**
     * 商品条码长度
     */
    public static final int LEN_BAR_CODE = 13;

    /**
     * 银行账号
     */
    public static final int LEN_BANK_ACCOUNT = 20;
    public static final int LEN_TAX = 20;
    public static final int LEN_ENUM = 30;
    public static final int LEN_TITLE = 100;
    public static final int LEN_SHORT_NAME = 16;
    public static final int LEN_COLOR = 8;


}
