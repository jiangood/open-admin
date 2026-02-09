package io.github.jiangood.openadmin.framework.config.security.login;

/**
 * 自定义登录逻辑
 *
 * 仿造 formLogin
 *
 *  三个基础类
 * 自定义一个Token：AbstractAuthenticationToken
 * Token解析器：AuthenticationConverter
 * 认证器：AuthenticationProvider
 *
 * 一个配置类：AjaxLoginConfigurer 用于配置
 * 一个过滤器：LoginFilter ， 用于拦截请求，处理登录认证
 *
 *
 *
 */