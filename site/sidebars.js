/** @type {import('@docusaurus/plugin-content-docs').SidebarsConfig} */
const sidebars = {
  tutorialSidebar: [
    {
      type: 'doc',
      label: '首页',
      id: 'index',
    },
    {
      type: 'category',
      label: '项目概述',
      items: [
        'architecture',
      ],
    },
    {
      type: 'category',
      label: '入门指南',
      items: [
        'quick-start',
      ],
    },
    {
      type: 'category',
      label: '核心功能',
      items: [
        'core-features/user-permission',
        'core-features/data-dict',
      ],
    },
    {
      type: 'category',
      label: 'API文档',
      items: [
        {
          type: 'category',
          label: '前端API',
          items: [
            'api/frontend/components',
            'api/frontend/field-components',
            'api/frontend/system-components',
            'api/frontend/utils',
          ],
        },
        {
          type: 'category',
          label: '后端API',
          items: [
            'api/backend/data-spec',
            'api/backend/utils',
            'api/backend/annotations',
            'api/backend/validators',
            'api/backend/job',
          ],
        },
      ],
    },
    {
      type: 'category',
      label: '开发指南',
      items: [
        'development/coding-standard',
        'development/best-practices',
        'development/agent',
      ],
    },
  ],
};

module.exports = sidebars;