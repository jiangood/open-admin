import js from '@eslint/js';
import tseslint from 'typescript-eslint';
import reactHooks from 'eslint-plugin-react-hooks';
import reactRefresh from 'eslint-plugin-react-refresh';
import reactCompiler from 'eslint-plugin-react-compiler';
import globals from 'globals';

export default tseslint.config(
    {ignores: ['dist', 'node_modules']},
    js.configs.recommended,
    ...tseslint.configs.recommended,
    {
        files: ['src/**/*.{ts,tsx,js,jsx}'],
        languageOptions: {
            globals: globals.browser,
        },
        plugins: {
            'react-hooks': reactHooks,
            'react-refresh': reactRefresh,
            'react-compiler': reactCompiler,
        },
        rules: {
            'react-hooks/rules-of-hooks': 'error',
            'react-hooks/exhaustive-deps': 'warn',
            'react-refresh/only-export-components': ['warn', {
                allowConstantExport: true,
                // 页面元信息是构建期约定，不参与 React 组件导出
                allowExportNames: ['pageMeta'],
            }],
            'react-compiler/react-compiler': 'error',
            '@typescript-eslint/no-unused-expressions': ['error', {allowShortCircuit: true}],
            '@typescript-eslint/no-unused-vars': ['error', {argsIgnorePattern: '^_', varsIgnorePattern: '^_', ignoreRestSiblings: true}],
        },
    },
);
