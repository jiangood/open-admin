-- =============================================================================
-- 文章管理 — 默认数据
-- =============================================================================

INSERT INTO sys_article (id, code, title, content, position, seq, enabled, create_time, update_time) VALUES
('article_about', 'about', '关于系统', '<h1>关于系统</h1><p>欢迎使用本系统。</p>', 'dropdown', 10, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO sys_article (id, code, title, content, position, seq, enabled, create_time, update_time) VALUES
('article_help', 'help', '系统帮助', '<h1>系统帮助</h1><p>系统使用帮助。</p>', 'dropdown', 20, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
