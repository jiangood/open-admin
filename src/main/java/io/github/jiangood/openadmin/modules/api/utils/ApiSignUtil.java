package io.github.jiangood.openadmin.modules.api.utils;

import lombok.experimental.UtilityClass;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

@UtilityClass
public class ApiSignUtil {
    /**
     * 新的md5签名，首尾放secret。
     *
     * @param params 传给服务器的参数
     * @param secret 分配给您的APP_SECRET
     * @throws NoSuchAlgorithmException
     * @throws UnsupportedEncodingException
     */
    public static String md5Signature(Map<String, String> params,
                                      String secret) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        TreeMap<String, String> treeMap = sort(params);
        String result = null;
        StringBuffer orgin = getBeforeSign(treeMap, new StringBuffer(secret));
        if (orgin == null) {
            return result;
        }
        orgin.append(secret);

        MessageDigest md = MessageDigest.getInstance(Constant.ENCODE_DECODE_MD5);
        result = byte2hex(md.digest(orgin.toString().getBytes(StandardCharsets.UTF_8)));

        return result;
    }

    /**
     * 二行制转字符串
     */
    private static String byte2hex(byte[] b) {
        StringBuffer hs = new StringBuffer();
        String stmp = Constant.EMPTY_STRING;
        for (int n = 0; n < b.length; n++) {
            stmp = (Integer.toHexString(b[n] & 0XFF));
            if (stmp.length() == 1) {
                hs.append(Constant.CHAR_NUM_0).append(stmp);
            } else {
                hs.append(stmp);
            }
        }
        return hs.toString().toUpperCase();
    }

    /**
     * 添加参数的封装方法
     *
     * @param params
     * @param orgin
     * @return
     */
    private static StringBuffer getBeforeSign(TreeMap<String, String> params,
                                              StringBuffer orgin) {
        if (params == null) {
            return null;
        }
        TreeMap<String, String> treeMap = sort(params);
        Iterator<String> iter = treeMap.keySet().iterator();
        while (iter.hasNext()) {
            String name = iter.next();
            orgin.append(name).append(params.get(name));
        }
        return orgin;
    }

    /**
     * 新的md5签名，hmac加密
     *
     * @param params 传给服务器的参数
     * @param secret 分配给您的APP_SECRET
     * @throws Exception
     * @throws UnsupportedEncodingException
     */
    public static String hmacSignature(TreeMap<String, String> params,
                                       String secret) throws UnsupportedEncodingException, Exception {
        TreeMap<String, String> treeMap = sort(params);
        String result = null;
        StringBuffer orgin = getBeforeSign(treeMap, new StringBuffer());
        if (orgin == null) {
            return result;
        }

        result = byte2hex(encryptHMAC(orgin.toString().getBytes(StandardCharsets.UTF_8),
                secret));

        return result;
    }

    /**
     * HMAC加密算法
     *
     * @param data
     * @param key
     * @return
     * @throws Exception
     */
    public static byte[] encryptHMAC(byte[] data, String key) throws Exception {
        SecretKey secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8),
                Constant.ENCODE_DECODE_HMACMD5);
        Mac mac = Mac.getInstance(secretKey.getAlgorithm());
        mac.init(secretKey);
        return mac.doFinal(data);
    }

    /**
     * map按键名正向排序
     *
     * @param map
     * @return
     */
    @SuppressWarnings("rawtypes")
    public static TreeMap<String, String> sort(Map<String, String> map) {
        TreeMap<String, String> mapVK = new TreeMap<String, String>(new Comparator<String>() {

            @Override
            public int compare(String obj1, String obj2) {
                String v1 = obj1;
                String v2 = obj2;
                return v1.compareTo(v2);
            }
        });

        for (Iterator i = map.keySet().iterator(); i.hasNext(); ) {
            String obj = (String) i.next();
            mapVK.put(obj, map.get(obj));
        }

        return mapVK;
    }

    /**
     * md5加密
     *
     * @param str
     * @return
     */
    public static String getMd5Str(String str) {
        MessageDigest messageDigest = null;

        try {
            messageDigest = MessageDigest.getInstance(Constant.ENCODE_DECODE_MD5);

            messageDigest.reset();

            messageDigest.update(str.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        byte[] byteArray = messageDigest.digest();

        StringBuffer md5StrBuff = new StringBuffer();

        for (int i = 0; i < byteArray.length; i++) {
            if (Integer.toHexString(0xFF & byteArray[i]).length() == 1) {
                md5StrBuff.append(Constant.CHAR_NUM_0).append(
                        Integer.toHexString(0xFF & byteArray[i]));
            } else {
                md5StrBuff.append(Integer.toHexString(0xFF & byteArray[i]));
            }
        }

        return md5StrBuff.toString();
    }

	/*public static void main(String[] args) throws Exception {
		//String secret = UUID.randomUUID().toString();
		//secret = secret.replaceAll("-", "");
		String str = "2f08d474c4e94bcaa4a9a61a182af854AppKeyb59be90963ce471fb5f1889e493a54ceMethodbank.order.pushTimestamp2019-03-15 16:45:29FormatJsonData{\"yhls\":\"088105831259\",\"orderId\":\"201903131616\",\"totalFee\":\"158.62\"}2f08d474c4e94bcaa4a9a61a182af854";
		MessageDigest md = MessageDigest.getInstance(Constant.ENCODE_DECODE_MD5);
		String result = byte2hex(md.digest(str.toString().getBytes(Constant.CHAR_ENCODING_UTF8)));
		System.out.println(result);
		// TreeMap<String, String> signMap = new TreeMap<String, String>();
		// signMap.put("Method", "testClass.test");
		// signMap.put("AppKey", "12qweqqw");
		// signMap.put("TimeStamp", String.valueOf(System.currentTimeMillis()));
		// signMap.put("Data",
		// "{\"loginName\":\"ZT013922\",\"code\":\"zhangsan\",\"phone\":\"13602535623\"}");
		// signMap.put("Format", "xml");
		// try {
		// String json = JsonUtils.toJson(signMap);
		// System.out.println(System.currentTimeMillis());
		// } catch (JsonProcessingException e1) {
		// // TODO Auto-generated catch block
		// e1.printStackTrace();
		// }
		// try {
		// System.out.print(md5Signature(signMap, "dcaf5378a148bfef028e17888c93756a"));
		// signMap.put("Sign", md5Signature(signMap, "dcaf5378a148bfef028e17888c93756a"));
		// String string = HttpTool.post("http://127.0.0.1:8080/api/gateway", signMap);
		// System.out.println(string);
		// } catch (NoSuchAlgorithmException e) {
		// e.printStackTrace();
		// } catch (UnsupportedEncodingException e) {
		// e.printStackTrace();
		// }
	}*/
}
