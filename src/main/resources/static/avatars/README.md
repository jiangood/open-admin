# 内置头像来源

本目录头像由 [Twemoji](https://github.com/jdecked/twemoji) 动物表情图形合成，统一加了彩色圆形背景。

- 图形作者：Twitter, Inc and other contributors
- 图形许可证：CC-BY 4.0（https://creativecommons.org/licenses/by/4.0/）
- 源码仓库：https://github.com/jdecked/twemoji
- 修改说明：将原始动物表情图形缩放后合成到 128×128 的彩色背景中，图形本身未修改

## 文件对应关系

| 文件 | 动物 | 对应生肖/元素 | 背景色 |
| --- | --- | --- | --- |
| `default.svg` | 大熊猫 🐼 | 中国国宝，默认头像 | `#d0f4de` |
| `avatar-1.svg` | 鼠 🐭 | 十二生肖·子鼠 | `#e0e7ff` |
| `avatar-2.svg` | 牛 🐮 | 十二生肖·丑牛 | `#d9f2e6` |
| `avatar-3.svg` | 虎 🐯 | 十二生肖·寅虎 | `#fef3c7` |
| `avatar-4.svg` | 兔 🐰 | 十二生肖·卯兔 | `#ffd5dc` |
| `avatar-5.svg` | 龙 🐲 | 十二生肖·辰龙 | `#ffdfbf` |
| `avatar-6.svg` | 蛇 🐍 | 十二生肖·巳蛇 | `#d0f4de` |
| `avatar-7.svg` | 马 🐴 | 十二生肖·午马 | `#e8f0fe` |
| `avatar-8.svg` | 羊 🐑 | 十二生肖·未羊 | `#f3e8ff` |
| `avatar-9.svg` | 猴 🐵 | 十二生肖·申猴 | `#fff3db` |
| `avatar-10.svg` | 鸡 🐔 | 十二生肖·酉鸡 | `#ffe4e6` |
| `avatar-11.svg` | 狗 🐶 | 十二生肖·戌狗 | `#e0f2fe` |
| `avatar-12.svg` | 猪 🐷 | 十二生肖·亥猪 | `#fce7f3` |

## 生成方式

原始 Twemoji 文件位于 `assets/svg/`，例如：

```
https://raw.githubusercontent.com/jdecked/twemoji/main/assets/svg/1f43c.svg  # 大熊猫
https://raw.githubusercontent.com/jdecked/twemoji/main/assets/svg/1f42f.svg  # 老虎
```

合成规则：128×128 画布 + 彩色背景 + 动物图形（`translate(22 22) scale(2.3333333)`）。

如需新增头像，建议保持同样尺寸和背景风格，并同步更新后端 `AvatarPresets`。
