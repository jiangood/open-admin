import { theme } from 'antd';
import type { ThemeConfig } from 'antd';

const { getDesignToken } = theme;

let cachedConfig: ThemeConfig | null = null;
let cachedToken: Record<string, any> | null = null;

function buildConfig(): ThemeConfig {
  const token: Record<string, any> = { borderRadius: 4 };
  const components: Record<string, any> = {
    Layout: { headerBg: 'white', triggerHeight: 32 },
  };

  try {
    const env = import.meta.env;
    if (env.VITE_THEME_PRIMARY_COLOR) token.colorPrimary = env.VITE_THEME_PRIMARY_COLOR;
    if (env.VITE_THEME_SUCCESS_COLOR) token.colorSuccess = env.VITE_THEME_SUCCESS_COLOR;
    if (env.VITE_THEME_WARNING_COLOR) token.colorWarning = env.VITE_THEME_WARNING_COLOR;
    if (env.VITE_THEME_ERROR_COLOR) token.colorError = env.VITE_THEME_ERROR_COLOR;
    if (env.VITE_THEME_BACKGROUND_COLOR) token.colorBgLayout = env.VITE_THEME_BACKGROUND_COLOR;
  } catch (e) {}

  return { token, components };
}

export function getThemeConfig(): ThemeConfig {
  if (!cachedConfig) {
    cachedConfig = buildConfig();
  }
  return cachedConfig;
}

export function getToken(): Record<string, any> {
  if (!cachedToken) {
    cachedToken = getDesignToken(getThemeConfig());
  }
  return cachedToken;
}
