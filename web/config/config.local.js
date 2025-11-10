// .umirc.local.ts 仅在 umi dev 时有效。umi build 时不会被加载
// 这份配置会和 .umirc.ts 做 deep merge 后形成最终配置。


import {defineConfig} from 'umi';
import {defaultConfigLocal} from "./defaultConfig";

export default defineConfig(defaultConfigLocal);
