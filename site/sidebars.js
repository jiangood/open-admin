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
        'quick-start',
        'architecture',
      ],
    },
    {
      type: 'category',
      label: '前端文档',
      items: [
        'frontend/components',
        'frontend/field-components',
        'frontend/system-components',
        'frontend/utils',
      ],
    },
    {
      type: 'category',
      label: '后端文档',
      items: [
        'backend/data-spec',
        'backend/utils',
        'backend/annotations',
        'backend/validators',
        'backend/job',
      ],
    },
    {
      type: 'category',
      label: '开发指南',
      items: [
        'guide/coding-standard',
        'guide/best-practices',
        'guide/agent',
      ],
    },
  ],
};

module.exports = sidebars;