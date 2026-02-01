import {themes as prismThemes} from 'prism-react-renderer';


const config = {
  title: 'open-admin',
  tagline: '小型管理系统框架，提供一整套前后端开箱即用的解决方案',

  future: {
    v4: true, // Improve compatibility with the upcoming Docusaurus v4
  },

  url: 'https://jiangood.github.io',
  baseUrl: '/open-admin/',

  organizationName: 'jiangood', // Usually your GitHub org/user name.
  projectName: 'open-admin', // Usually your repo name.

  onBrokenLinks: 'warn',

  // Control whether URLs have trailing slashes.
  // For GitHub pages, it's recommended to set this to true
  trailingSlash: true,

  // Set the favicon of your site
  favicon: 'img/logo.svg',



  presets: [
    [
      'classic',

      {
        docs: {
          routeBasePath: '/',
          sidebarPath: './sidebars.js',
          editUrl: 'https://github.com/jiangood/open-admin/tree/master/site/',
        },
        blog:false,

        theme: {
          customCss: './src/css/custom.css',
        },
      },
    ],
  ],

  themeConfig:
      ({
        // Replace with your project's social card
        image: 'img/logo.svg',
        navbar: {
          title: 'open-admin',
          logo: {
            alt: 'open-admin Logo',
            src: 'img/logo.svg',
            // Add logo for dark mode if needed
            // srcDark: 'img/logo-dark.svg',
          },
          items: [
            {
              type: 'docSidebar',
              sidebarId: 'tutorialSidebar',
              position: 'left',
              label: '文档',
            },
            {
              href: 'https://github.com/jiangood/open-admin',
              label: 'GitHub',
              position: 'right',
              // Add GitHub icon
              className: 'navbar-github-link',
            },
          ],
        },

        prism: {
          theme: prismThemes.github,
          darkTheme: prismThemes.dracula,
          additionalLanguages: ['java', 'javascript', 'typescript', 'sql', 'yaml', 'json'],

        },
      }),
};

export default config;
