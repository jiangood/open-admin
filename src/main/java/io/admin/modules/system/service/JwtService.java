package io.admin.modules.system.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import cn.hutool.jwt.JWTValidator;
import cn.hutool.jwt.RegisteredPayload;
import io.admin.common.utils.RequestTool;
import io.admin.modules.system.dao.SysJwtDao;
import io.admin.modules.system.entity.SysJwt;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.Map;


@Service
public class JwtService {


    public static final String CONFIG_KEY = "sys.jwtSecret";


    public String createToken(String subject, Date expireTime) {
        return createToken(subject, expireTime, null);
    }

    /**
     * @param subject 一般是用户id，员工id，客户id等
     * @return
     */
    public String createToken(String subject, Date expireTime, Map<String, Object> data) {
        JWT jwt = JWT.create()
                .setExpiresAt(DateUtil.date())
                .setSubject(subject)
                .setJWTId(IdUtil.fastSimpleUUID()) // 加个随机数，确保生成的token唯一
                .setExpiresAt(expireTime)
                .setKey(getJwtSecret());

        if (CollUtil.isNotEmpty(data)) {
            jwt.addPayloads(data);
        }


        String token = jwt.sign();
        SysJwt sysJwt = new SysJwt();
        sysJwt.setTokenMd5(SecureUtil.md5(token));
        sysJwt.setDisabled(false);
        sysJwt.setExpireTime(expireTime);
        sysJwtDao.save(sysJwt);

        return token;
    }


    public JwtInfo getJwt(String token) {
        if (token != null) {
            JWT jwt = JWTUtil.parseToken(token);
            JwtInfo info = new JwtInfo();
            String subject = (String) jwt.getPayload(RegisteredPayload.SUBJECT);
            info.setSubject(subject);
            info.setData(jwt.getPayloads());
            return info;
        }
        return null;
    }

    public JwtInfo getJwt() {
        HttpServletRequest request = RequestTool.currentRequest();
        String token = getToken(request);
        return getJwt(token);
    }

    public String getSubject() {
        HttpServletRequest request = RequestTool.currentRequest();
        String token = getToken(request);
        return getJwt(token).getSubject();
    }

    public String getToken(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null) {
            token = token.replace("Bearer ", "");
        }
        return token;
    }

    /**
     * 禁用token，相当于踢人
     *
     * @return
     */
    @Transactional
    public void disableToken(String token) {
        SysJwt sysJwt = sysJwtDao.findByTokenMd5(SecureUtil.md5(token));
        if (sysJwt != null) {
            sysJwt.setDisabled(true);
            sysJwt.setDisabledTime(DateUtil.date());
            sysJwtDao.save(sysJwt);
        }
    }

    public byte[] getJwtSecret() {
        if (jwtSecretBytes == null) {
            String jwtSecret = sysConfigService.get(CONFIG_KEY);
            Assert.hasText(jwtSecret, "未配置系统参数" + CONFIG_KEY);
            jwtSecretBytes = jwtSecret.trim().getBytes();

        }
        return jwtSecretBytes;
    }

    public void verify(String token) {
        byte[] jwtSecret = getJwtSecret();
        boolean verify = JWTUtil.verify(token, jwtSecret);
        Assert.state(verify, "token不合法");
        try {
            JWTValidator.of(token).validateDate();
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage());
        }

        // 最后再校验是否被踢出去
        SysJwt sysJwt = sysJwtDao.findByTokenMd5(SecureUtil.md5(token));
        if (sysJwt != null) {
            Assert.state(!sysJwt.getDisabled(), "当前登录凭证已失效");
        }
    }


    private byte[] jwtSecretBytes;

    @Resource
    private SysConfigService sysConfigService;

    @Resource
    private SysJwtDao sysJwtDao;


    @Getter
    @Setter
    public static class JwtInfo {
        String subject;
        Map<String, Object> data;
    }
}
