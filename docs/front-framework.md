
## 工具类 (utils)

### `ArrUtils`

*   **工具类名称**: `ArrUtils`
*   **描述**: 提供了一系列对数组进行操作的静态方法，包括检查、添加、移除、清空、截取、交换、去重以及查找最大值等。
*   **方法**:
    *   `contains<T>(arr: T[], item: T): boolean`: 检查数组是否包含某个元素。
    *   `containsAny<T>(arr: T[], ...items: T[]): boolean`: 检查数组是否包含至少一个指定的元素。
    *   `add<T>(arr: T[], item: T): void`: 在数组末尾添加一个元素。
    *   `addAt<T>(arr: T[], index: number, item: T): void`: 在数组的指定索引处添加一个元素。
    *   `addAll<T>(arr: T[], items: T[]): void`: 将另一个数组的所有元素追加到目标数组的尾部。
    *   `removeAt<T>(arr: T[], index: number): void`: 移除数组指定索引处的元素。
    *   `remove<T>(arr: T[], item: T): void`: 移除数组中第一个匹配的元素。
    *   `clear<T>(arr: T[]): void`: 清空数组。
    *   `sub<T>(arr: T[], fromIndex?: number, toIndex?: number): T[]`: 截取数组的一个子集。
    *   `swap<T>(arr: T[], item1: T, item2: T): void`: 交换数组中两个元素的位置。
    *   `insert<T>(arr: T[], index: number, item: T): void`: 在数组的指定索引处插入一个元素 (与 `addAt` 相同，保留以兼容原 API)。
    *   `pushIfNotExist<T>(arr: T[], item: T): void`: 如果元素不存在于数组中，则将其添加到数组末尾。
    *   `pushAll<T>(arr: T[], newArr: T[]): void`: 将新数组中的所有元素添加到目标数组的末尾。
    *   `maxBy<T extends Record<K, any>, K extends keyof T>(arr: T[], key: K): T | undefined`: 获取对象数组中某一属性值最大的对象。
    *   `unique<T>(arr: T[]): T[]`: 对数组进行去重。
*   **示例**:

    ```typescript
    import { ArrUtils } from '@/framework/utils';

    let numbers = [1, 2, 3, 2, 4];
    console.log('原始数组:', numbers);

    ArrUtils.add(numbers, 5);
    console.log('添加元素5:', numbers); // [1, 2, 3, 2, 4, 5]

    ArrUtils.addAt(numbers, 1, 99);
    console.log('在索引1添加99:', numbers); // [1, 99, 2, 3, 2, 4, 5]

    ArrUtils.remove(numbers, 2);
    console.log('移除元素2 (第一次出现):', numbers); // [1, 99, 3, 2, 4, 5]

    ArrUtils.removeAt(numbers, 0);
    console.log('移除索引0的元素:', numbers); // [99, 3, 2, 4, 5]

    let uniqueNumbers = ArrUtils.unique(numbers);
    console.log('去重后的数组:', uniqueNumbers); // [99, 3, 2, 4, 5]

    let otherNumbers = [10, 20];
    ArrUtils.addAll(numbers, otherNumbers);
    console.log('添加另一个数组:', numbers); // [99, 3, 2, 4, 5, 10, 20]

    interface User { id: number; score: number; name: string; }
    const users: User[] = [
        { id: 1, score: 90, name: 'Alice' },
        { id: 2, score: 95, name: 'Bob' },
        { id: 3, score: 88, name: 'Charlie' },
    ];
    const maxUser = ArrUtils.maxBy(users, 'score');
    console.log('分数最高的用户:', maxUser); // { id: 2, score: 95, name: 'Bob' }

    let swapArr = ['a', 'b', 'c'];
    ArrUtils.swap(swapArr, 'a', 'c');
    console.log('交换a和c:', swapArr); // ['c', 'b', 'a']

    let newArr = [1, 2, 3];
    ArrUtils.pushIfNotExist(newArr, 4);
    console.log('pushIfNotExist 4:', newArr); // [1, 2, 3, 4]
    ArrUtils.pushIfNotExist(newArr, 2);
    console.log('pushIfNotExist 2 (已存在):', newArr); // [1, 2, 3, 4]
    ```

### `ColorsUtils`

*   **工具类名称**: `ColorsUtils`
*   **描述**: 提供了一系列颜色相关的转换和操作方法，包括 RGB 和 HSV 颜色模型之间的转换、十六进制和 RGB 字符串的相互转换、颜色调亮/调暗、亮度/感知亮度计算以及颜色混合等。
*   **接口**:
    *   `RGB`: `{ r: number, g: number, b: number, a?: number }` (r, g, b 范围 [0, 255]; a 范围 [0, 100] 百分比)。
    *   `HSV`: `{ h: number, s: number, v: number, a?: number }` (h 范围 [0, 360]; s, v, a 范围 [0, 100] 百分比)。
*   **方法**:
    *   `rgbToHex(color: RGB): string`: 将 RGB 颜色对象转换为十六进制字符串 (包含可选的 alpha 值)。
    *   `rgbToString(color: RGB): string`: 将 RGB 颜色对象转换为 `rgb()` 或 `rgba()` 字符串。
    *   `hexToRgb(hex: string): RGB`: 将十六进制颜色字符串转换为 RGB 颜色对象。支持 '#RGB', '#RGBA', '#RRGGBB', '#RRGGBBAA' 格式。
    *   `hsvToRgb(color: HSV): RGB`: 将 HSV 颜色对象转换为 RGB 颜色对象。
    *   `rgbToHsv(color: RGB): HSV`: 将 RGB 颜色对象转换为 HSV 颜色对象。
    *   `textToRgb(str: string): RGB`: 将颜色文本 (hex 或 rgb/rgba) 转换为 RGB 颜色对象。
    *   `lighten(color: string, percent: number): string`: 调亮或调暗颜色。`percent > 0` 为调亮，`percent < 0` 为调暗。
    *   `luminosity(color: string | RGB): number`: 计算颜色的亮度 (Luminosity)。
    *   `brightness(color: string | RGB): number`: 计算颜色的感知亮度 (Brightness)。
    *   `blend(fgColor: string | RGB, bgColor: string | RGB): string | RGB`: 将前景颜色 (fgColor) 混合到背景颜色 (bgColor) 上。
    *   `changeAlpha(color: string, offset: number): string`: 改变颜色字符串的 alpha 透明度。
*   **示例**:

    ```typescript
    import { ColorsUtils } from '@/framework/utils';

    // RGB 到 Hex 转换
    const rgbColor = { r: 255, g: 0, b: 0 };
    console.log('RGB转Hex:', ColorsUtils.rgbToHex(rgbColor)); // #ff0000

    // Hex 到 RGB 转换
    const hexColor = '#00FF00';
    console.log('Hex转RGB:', ColorsUtils.hexToRgb(hexColor)); // { r: 0, g: 255, b: 0 }

    // RGB 到 String 转换
    const rgbaColor = { r: 0, g: 0, b: 255, a: 50 }; // 50% alpha
    console.log('RGBA转String:', ColorsUtils.rgbToString(rgbaColor)); // rgba(0,0,255,0.5)

    // 调亮颜色
    const originalColor = '#663399';
    const lighterColor = ColorsUtils.lighten(originalColor, 20); // 调亮20%
    console.log('调亮:', lighterColor);

    // 改变 Alpha
    const colorWithAlpha = ColorsUtils.changeAlpha('#0000FF', -0.5); // 减少50%透明度
    console.log('改变Alpha:', colorWithAlpha);

    // 亮度
    console.log('亮度:', ColorsUtils.luminosity('#FFFFFF')); // 1
    console.log('亮度:', ColorsUtils.luminosity('#000000')); // 0

    // 感知亮度
    console.log('感知亮度:', ColorsUtils.brightness('#FFFFFF')); // 255
    console.log('感知亮度:', ColorsUtils.brightness('#000000')); // 0
    ```

### `DateUtils`

*   **工具类名称**: `DateUtils`
*   **描述**: 提供了一系列对日期和时间进行操作的静态方法，包括获取年、月、日、时、分、秒，以及多种日期时间格式化，友好时间显示和时间段显示等。
*   **方法**:
    *   `year(date: Date): number`: 获取日期的年份。
    *   `month(date: Date): string`: 获取日期的月份，自动补0 (例如 "01" 表示一月)。
    *   `date(date: Date): string`: 获取日期的天，自动补0 (例如 "05" 表示5号)。
    *   `hour(date: Date): string`: 获取小时 (24进制)，自动补0。
    *   `minute(date: Date): string`: 获取分钟，自动补0。
    *   `second(date: Date): string`: 获取秒，自动补0。
    *   `formatDate(d: Date): string`: 格式化日期为 "YYYY-MM-DD" 格式。
    *   `formatTime(d: Date): string`: 格式化时间为 "HH:mm:ss" 格式。
    *   `formatDateTime(d: Date): string`: 格式化日期时间为 "YYYY-MM-DD HH:mm:ss" 格式。
    *   `formatDateCn(d: Date): string`: 格式化日期为 "YYYY年M月D日" 格式。
    *   `now(): string`: 获取当前日期时间，格式为 "YYYY-MM-DD HH:mm:ss"。
    *   `today(): string`: 获取当前日期，格式为 "YYYY-MM-DD"。
    *   `thisYear(): number`: 获取当前年份。
    *   `thisMonth(): string`: 获取当前月份 (自动补0)。
    *   `friendlyTime(pastDate: Date | string | number): string | undefined`: 显示友好时间，如 "2小时前", "1周前"。
    *   `friendlyTotalTime(time: number | string | null): string | null`: 格式化总耗时，如 "3分5秒"。
    *   `beginOfMonth(): string`: 获取当前月的第一天日期，格式为 "YYYY-MM-DD"。
*   **示例**:

    ```typescript
    import { DateUtils } from '@/framework/utils';

    const now = new Date();
    const past = new Date(now.getTime() - (3 * 24 * 60 * 60 * 1000 + 5 * 60 * 1000)); // 3天5分钟前

    console.log('当前年份:', DateUtils.thisYear());
    console.log('当前月份:', DateUtils.thisMonth());
    console.log('当前日期:', DateUtils.today());
    console.log('当前日期时间:', DateUtils.now());

    console.log('格式化日期:', DateUtils.formatDate(now));
    console.log('格式化时间:', DateUtils.formatTime(now));
    console.log('格式化日期时间:', DateUtils.formatDateTime(now));
    console.log('中文格式日期:', DateUtils.formatDateCn(now));

    console.log('友好时间 (3天5分钟前):', DateUtils.friendlyTime(past));
    console.log('友好时间 (现在):', DateUtils.friendlyTime(now));

    console.log('总耗时 (185秒):', DateUtils.friendlyTotalTime(185000)); // 185000 毫秒 = 3分5秒
    console.log('本月第一天:', DateUtils.beginOfMonth());
    ```

### `DeviceUtils`

*   **工具类名称**: `DeviceUtils`
*   **描述**: 包含了常用的浏览器和网络工具函数，用于判断设备类型和获取 WebSocket 的基础 URL。
*   **方法**:
    *   `isMobileDevice(): boolean`: 判断当前设备是否为移动设备 (包括 Windows Phone, Android, iOS)。
    *   `getWebsocketBaseUrl(): string`: 根据当前页面的协议 (http/https) 和主机名构造 WebSocket 的基础 URL (ws/wss)。
*   **示例**:

    ```typescript
    import { DeviceUtils } from '@/framework/utils';

    if (DeviceUtils.isMobileDevice()) {
      console.log('当前设备是移动设备。');
    } else {
      console.log('当前设备不是移动设备。');
    }

    const websocketUrl = DeviceUtils.getWebsocketBaseUrl();
    console.log('WebSocket 基础 URL:', websocketUrl);
    ```

### `DomUtils`

*   **工具类名称**: `DomUtils`
*   **描述**: 封装了获取元素位置和尺寸的工具方法。
*   **方法**:
    *   `offset(el: Element | Window): { top: number; left: number }`: 获取元素相对于视口的偏移量 (top 和 left)。
    *   `height(el: Element | Window): number`: 获取元素的外部高度 (如果是 `window`，则返回视口高度)。
    *   `width(el: Element | Window): number`: 获取元素的外部宽度 (如果是 `window`，则返回视口宽度)。
*   **示例**:

    ```typescript
    import { DomUtils } from '@/framework/utils';

    // 假设有一个 ID 为 'myElement' 的 DOM 元素
    const myElement = document.getElementById('myElement');

    if (myElement) {
      const elementOffset = DomUtils.offset(myElement);
      console.log('元素偏移量:', elementOffset); // { top: ..., left: ... }

      const elementHeight = DomUtils.height(myElement);
      console.log('元素高度:', elementHeight);

      const elementWidth = DomUtils.width(myElement);
      console.log('元素宽度:', elementWidth);
    }

    // 获取窗口尺寸
    console.log('窗口高度:', DomUtils.height(window));
    console.log('窗口宽度:', DomUtils.width(window));
    ```

### `EventBusUtils`

*   **工具类名称**: `EventBusUtils`
*   **描述**: 这是一个静态事件总线工具类，提供事件的发布与订阅机制。所有方法和状态均为静态，无需实例化即可直接使用。
*   **方法**:
    *   `on<T extends any[] = []>(name: string, callback: (...args: T) => void, ctx?: any): void`: 注册事件监听器。
    *   `once<T extends any[] = []>(name: string, callback: (...args: T) => void, ctx?: any): void`: 注册只触发一次的事件监听器。事件被触发后，该监听器会自动移除。
    *   `emit<T extends any[] = []>(name: string, ...args: T): void`: 触发事件，并向所有注册的监听器传递参数。
    *   `off<T extends any[] = []>(name: string, callback?: ((...args: T) => void)): void`: 移除事件监听器。如果 `callback` 参数省略，则移除该事件名的所有监听器。
*   **示例**:

    ```typescript
    import { EventBusUtils } from '@/framework/utils';

    // 1. 注册普通事件监听器
    EventBusUtils.on('userLoggedIn', (username: string, timestamp: number) => {
      console.log(`用户 ${username} 在 ${new Date(timestamp).toLocaleString()} 登录了。`);
    });

    // 2. 注册只触发一次的事件监听器
    EventBusUtils.once('firstLoginPrompt', () => {
      console.log('欢迎第一次登录！');
    });

    // 3. 触发事件
    EventBusUtils.emit('userLoggedIn', 'Alice', Date.now());
    EventBusUtils.emit('firstLoginPrompt'); // 第一次触发
    EventBusUtils.emit('userLoggedIn', 'Bob', Date.now());
    EventBusUtils.emit('firstLoginPrompt'); // 第二次触发，不会有输出，因为once事件已移除

    // 4. 移除特定监听器
    const logoutHandler = (username: string) => {
      console.log(`${username} 已注销。`);
    };
    EventBusUtils.on('userLoggedOut', logoutHandler);
    EventBusUtils.emit('userLoggedOut', 'Charlie');
    EventBusUtils.off('userLoggedOut', logoutHandler); // 移除特定监听器
    EventBusUtils.emit('userLoggedOut', 'David'); // David 的注销消息不会被打印

    // 5. 移除某个事件的所有监听器
    EventBusUtils.on('dataUpdated', (data: any) => console.log('数据更新1:', data));
    EventBusUtils.on('dataUpdated', (data: any) => console.log('数据更新2:', data));
    EventBusUtils.emit('dataUpdated', { status: 'success' });
    EventBusUtils.off('dataUpdated'); // 移除所有 dataUpdated 监听器
    EventBusUtils.emit('dataUpdated', { status: 'failed' }); // 不会有输出
    ```

### `MessageUtils`

*   **工具类名称**: `MessageUtils`
*   **描述**: 封装了 Ant Design 的 `Modal`、`message` 和 `notification` 静态方法，提供了弹出提示框、确认框、输入框、全局消息以及通知提醒框的功能。
*   **方法**:
    *   **Antd Modal 封装 (Alert/Confirm/Prompt)**:
        *   `alert(content: React.ReactNode, config?: Omit<ModalFuncProps, 'content' | 'icon' | 'onOk' | 'onCancel'>)`: 弹出 Alert 提示框。
        *   `confirm(content: React.ReactNode, config?: Omit<ModalFuncProps, 'content' | 'icon' | 'onOk' | 'onCancel'>): Promise<boolean>`: 弹出 Confirm 确认框。
        *   `prompt(message: React.ReactNode, initialValue?: string, placeholder?: string, config?: Omit<ModalFuncProps, 'content' | 'title' | 'icon' | 'onOk'>): Promise<string | undefined>`: 弹出 Prompt 输入框对话框。
    *   **Antd message 封装 (全局通知/Loading)**:
        *   `success(content: React.ReactNode, duration?: number)`: 成功消息。
        *   `error(content: React.ReactNode, duration?: number)`: 错误消息。
        *   `warning(content: React.ReactNode, duration?: number)`: 警告消息。
        *   `info(content: React.ReactNode, duration?: number)`: 通用消息。
        *   `loading(content?: React.ReactNode, duration?: number)`: 弹出 Loading 提示。
        *   `hideAll()`: 立即关闭所有 `message` 提示。
    *   **Antd Notification 封装 (通知提醒框)**:
        *   `notify(message: React.ReactNode, description: React.ReactNode, type?: 'success' | 'error' | 'info' | 'warning' | 'open' | 'config', placement?: NotificationPlacement, config?: Omit<ArgsProps, 'message' | 'description' | 'placement'>)`: 弹出通知提醒框。
        *   `notifySuccess(message: React.ReactNode, description: React.ReactNode, placement?: NotificationPlacement, config?: Omit<ArgsProps, 'message' | 'description' | 'placement'>)`: 弹出成功通知。
        *   `notifyError(message: React.ReactNode, description: React.ReactNode, placement?: NotificationPlacement, config?: Omit<ArgsProps, 'message' | 'description' | 'placement'>)`: 弹出失败通知。
        *   `notifyWarning(message: React.ReactNode, description: React.ReactNode, placement?: NotificationPlacement, config?: Omit<ArgsProps, 'message' | 'description' | 'placement'>)`: 弹出警告通知。
*   **示例**:

    ```tsx
    import React from 'react';
    import { Button, Space } from 'antd';
    import { MessageUtils } from '@/framework/utils';

    const MyComponent = () => {
      const showAlert = () => {
        MessageUtils.alert('这是一个普通的提示消息！');
      };

      const showConfirm = async () => {
        const result = await MessageUtils.confirm('确定要执行此操作吗？');
        console.log('确认结果:', result);
        MessageUtils.info(`你点击了: ${result ? '确定' : '取消'}`);
      };

      const showPrompt = async () => {
        const inputValue = await MessageUtils.prompt('请输入你的名字:', '张三', '你的名字');
        console.log('输入的值:', inputValue);
        MessageUtils.info(`你输入了: ${inputValue || '没有输入'}`);
      };

      const showSuccessMessage = () => {
        MessageUtils.success('操作成功！');
      };

      const showErrorMessage = () => {
        MessageUtils.error('操作失败，请重试。');
      };

      const showLoading = () => {
        const hide = MessageUtils.loading('正在处理中...', 0); // duration 0 表示不自动关闭
        setTimeout(() => {
          hide(); // 手动关闭
          MessageUtils.success('处理完成！');
        }, 2000);
      };

      const showNotification = () => {
        MessageUtils.notifySuccess('标题', '这是一条成功的通知消息！', 'bottomRight');
        MessageUtils.notifyError('错误提示', '数据加载失败。', 'topLeft', { duration: 5 });
      };

      return (
        <Space direction="vertical" style={{ width: '100%' }}>
          <Button onClick={showAlert}>显示 Alert</Button>
          <Button onClick={showConfirm}>显示 Confirm</Button>
          <Button onClick={showPrompt}>显示 Prompt</Button>
          <Button onClick={showSuccessMessage}>显示 Success Message</Button>
          <Button onClick={showErrorMessage}>显示 Error Message</Button>
          <Button onClick={showLoading}>显示 Loading Message</Button>
          <Button onClick={showNotification}>显示 Notification</Button>
        </Space>
      );
    };

    export default MyComponent;
    ```

### `ObjectUtils`

*   **工具类名称**: `ObjectUtils`
*   **描述**: 提供了一系列对对象进行操作的静态方法，例如安全地获取嵌套属性和复制属性。
*   **方法**:
    *   `get<TObj extends object, TDefault = unknown>(obj: TObj | null | undefined, path: string | (keyof TObj)[], defaultValue?: TDefault): unknown | TDefault`: 安全地获取深度嵌套的对象属性的值。
    *   `copyPropertyIfPresent<TSource extends object, TTarget extends object>(source: TSource | null | undefined, target: TTarget | null | undefined): void`: 复制对象属性，仅复制源对象中 **存在** 且目标对象中 **也有** 对应属性的那些值。
    *   `copyProperty<TSource extends object, TTarget extends object>(source: TSource | null | undefined, target: TTarget | null | undefined): void`: 复制对象属性，将源对象中 **非 undefined** 的属性值复制到目标对象对应的属性上。
*   **示例**:

    ```typescript
    import { ObjectUtils } from '@/framework/utils';

    const myObject = {
      user: {
        id: 123,
        name: 'Alice',
        address: {
          city: 'New York',
          zip: '10001'
        }
      },
      roles: ['admin', 'editor'],
      settings: null,
      active: true,
    };

    // get 方法示例
    console.log('获取 user.name:', ObjectUtils.get(myObject, 'user.name', 'Guest')); // Alice
    console.log('获取 user.address.city:', ObjectUtils.get(myObject, 'user.address.city')); // New York
    console.log('获取 roles[0]:', ObjectUtils.get(myObject, 'roles.0')); // admin (需要注意这里简化了数组索引的路径解析)
    console.log('获取 settings (null):', ObjectUtils.get(myObject, 'settings', 'No Settings')); // No Settings
    console.log('获取不存在的属性:', ObjectUtils.get(myObject, 'user.profile.age', 18)); // 18
    console.log('获取不存在的属性 (无默认值):', ObjectUtils.get(myObject, 'nonExistent')); // undefined


    // copyPropertyIfPresent 示例
    const sourceData = {
      name: 'Bob',
      age: 30,
      city: 'Los Angeles',
      id: 456,
    };
    const targetObject1 = {
      id: 0,
      name: '',
      active: false,
      email: '', // targetObject1 中有，sourceData 中没有
    };
    ObjectUtils.copyPropertyIfPresent(sourceData, targetObject1);
    console.log('copyPropertyIfPresent 结果:', targetObject1);
    // 期望: { id: 456, name: 'Bob', active: false, email: '' } - 只复制了 id, name，因为 targetObject1 没有 age 和 city，sourceData 没有 email。


    // copyProperty 示例
    const sourceUpdates = {
      name: 'Charlie',
      email: 'charlie@example.com',
      age: undefined, // undefined 的值不应该被复制
      status: null, // null 的值应该被复制
    };
    const targetObject2 = {
      id: 789,
      name: 'Original Name',
      email: 'original@example.com',
      status: 'active',
      city: 'Chicago', // targetObject2 中有，sourceUpdates 中没有
    };
    ObjectUtils.copyProperty(sourceUpdates, targetObject2);
    console.log('copyProperty 结果:', targetObject2);
    // 期望: { id: 789, name: 'Charlie', email: 'charlie@example.com', status: null, city: 'Chicago' }
    // age (undefined) 未复制，city 未受影响。
    ```

### `StorageUtils`

*   **工具类名称**: `StorageUtils`
*   **描述**: 使用 `localStorage` 进行数据的存取，并包含时间戳和默认值处理。存储时会将数据封装在一个包含 `createTime` 和 `data` 的对象中。
*   **方法**:
    *   `get<T>(key: string, defaultValue?: T | null): T | null`: 从 `localStorage` 获取数据。
    *   `set<T>(key: string, value: T | null | undefined): void`: 将数据存储到 `localStorage`。如果值为 `null` 或 `undefined`，则删除该键。
*   **示例**:

    ```typescript
    import { StorageUtils } from '@/framework/utils';

    interface UserData {
      username: string;
      token: string;
    }

    // 存储数据
    StorageUtils.set('currentUser', { username: 'Alice', token: 'abc123xyz' });
    StorageUtils.set('lastVisitedPage', '/dashboard');
    StorageUtils.set('rememberMe', true);

    // 获取数据
    const user = StorageUtils.get<UserData>('currentUser');
    console.log('当前用户:', user); // { username: 'Alice', token: 'abc123xyz', createTime: '...' }

    const lastPage = StorageUtils.get<string>('lastVisitedPage', '/home');
    console.log('上次访问页面:', lastPage); // /dashboard

    const rememberMe = StorageUtils.get<boolean>('rememberMe', false);
    console.log('记住我:', rememberMe); // true

    const nonExistent = StorageUtils.get('nonExistentKey', 'default value');
    console.log('不存在的键 (带默认值):', nonExistent); // default value

    // 删除数据 (通过设置值为 null 或 undefined)
    StorageUtils.set('lastVisitedPage', null);
    console.log('删除后再次获取:', StorageUtils.get('lastVisitedPage', '')); // ''
    ```

### `StringUtils`

*   **工具类名称**: `StringUtils`
*   **描述**: 提供了一系列对字符串进行操作的静态方法，包括移除前缀/后缀、生成随机字符串、空值处理、包含检查、计数、大小写转换、字符串反转、截取、补零、简单加解密、计算显示宽度、省略处理、驼峰/下划线命名转换、忽略大小写比较、以及字符串分割和连接。
*   **方法**:
    *   `removePrefix(str: string | null | undefined, ch: string): string | null | undefined`: 移除字符串前缀。
    *   `removeSuffix(str: string | null | undefined, ch: string): string | null | undefined`: 移除字符串后缀。
    *   `random(length: number): string`: 生成指定长度的随机字符串。
    *   `nullText(key: string | null | undefined): string`: 处理空值，返回 "key未定义"。
    *   `contains(str: string | null | undefined, subStr: string): boolean`: 检查字符串是否包含子字符串。
    *   `count(str: string | null | undefined, subStr: string): number`: 统计子字符串在原始字符串中出现的次数。
    *   `capitalize(str: string | null | undefined): string | null | undefined`: 将字符串的首字母转换为大写。
    *   `reverse(str: string | null | undefined): string | null | undefined`: 颠倒字符串的顺序。
    *   `subAfter(source: string | null | undefined, str: string): string | null | undefined`: 截取字符串，返回子字符串后面部分。
    *   `subAfterLast(source: string | null | undefined, str: string): string | null | undefined`: 截取字符串，返回最后一个子字符串后面的部分。
    *   `subBefore(s: string | null | undefined, sub: string): string | null | undefined`: 截取字符串，返回子字符串前面的部分。
    *   `obfuscateString(str: string | null | undefined): string | null | undefined`: 混淆字符串 (通过在每个字符后添加 '1')。
    *   `pad(input: string | number | null | undefined, totalLen: number, padChar?: string): string`: 补零或补指定字符。
    *   `encrypt(str: string | null | undefined): string | null | undefined`: 简单加密：将每个字符的 ASCII 码加 1，并转换为四位十六进制表示。
    *   `decrypt(hexString: string | null | undefined): string | null | undefined`: 简单解密：还原加密字符串。
    *   `getWidth(str: string | null | undefined): number`: 获取字符串的显示宽度：英文字符 1 宽，中文字符 2 宽。
    *   `cutByWidth(str: string, maxWidth: number): string`: 按显示宽度截取字符串。
    *   `ellipsis(str: string | null | undefined, len: number, suffix?: string): string | null | undefined`: 字符串省略处理 (按显示宽度计算)。
    *   `isStr(value: any): value is string`: 判断值是否为字符串类型。
    *   `toCamelCase(str: string, firstLower?: boolean): string`: 将下划线或连字符分隔的字符串转为驼峰命名。
    *   `toUnderlineCase(name: string | null | undefined): string | null | undefined`: 将驼峰命名字符串转为下划线命名。
    *   `equalsIgnoreCase(a: string | null | undefined, b: string | null | undefined): boolean`: 比较两个字符串是否相等，忽略大小写。
    *   `split(str: any, sp: string): null | string[]`: 分割字符串。
    *   `join(arr: any, sp: string): string | string[]`: 连接字符串。
*   **示例**:

    ```typescript
    import { StringUtils } from '@/framework/utils';

    console.log('移除前缀:', StringUtils.removePrefix('prefix_hello', 'prefix_')); // hello
    console.log('移除后缀:', StringUtils.removeSuffix('hello_suffix', '_suffix')); // hello
    console.log('随机字符串 (10位):', StringUtils.random(10)); // 随机输出

    console.log('空值文本:', StringUtils.nullText('名称')); // 名称未定义
    console.log('包含子串:', StringUtils.contains('hello world', 'world')); // true
    console.log('统计子串:', StringUtils.count('ababa', 'aba')); // 1 (注意重叠不计)

    console.log('首字母大写:', StringUtils.capitalize('hello')); // Hello
    console.log('字符串反转:', StringUtils.reverse('hello')); // olleh

    console.log('subAfter:', StringUtils.subAfter('hello-world', '-')); // world
    console.log('subAfterLast:', StringUtils.subAfterLast('hello-world-again', '-')); // again
    console.log('subBefore:', StringUtils.subBefore('hello-world', '-')); // hello

    const original = 'secret';
    const encrypted = StringUtils.encrypt(original);
    console.log('加密:', encrypted);
    console.log('解密:', StringUtils.decrypt(encrypted));

    console.log('字符串显示宽度 (Hello世界):', StringUtils.getWidth('Hello世界')); // 9
    console.log('按宽度截取 (Hello世界, 5):', StringUtils.cutByWidth('Hello世界', 5)); // Hello
    console.log('省略处理 (Hello世界, 5):', StringUtils.ellipsis('Hello世界', 5)); // Hello...

    console.log('驼峰转换:', StringUtils.toCamelCase('some_variable-name')); // someVariableName
    console.log('下划线转换:', StringUtils.toUnderlineCase('someVariableName')); // some_variable_name

    console.log('忽略大小写比较:', StringUtils.equalsIgnoreCase('Hello', 'hello')); // true

    console.log('分割字符串:', StringUtils.split('a,b,c', ',')); // ['a', 'b', 'c']
    console.log('连接字符串:', StringUtils.join(['x', 'y', 'z'], '-')); // x-y-z
    ```

### `TreeUtils`

*   **工具类名称**: `TreeUtils`
*   **描述**: 提供了一系列用于将数组和树结构互相转换，以及遍历、查找树节点的功能。
*   **接口**:
    *   `TreeNode<T = any>`: 通用树节点接口，包含 `id`, `pid`, `children` 字段，并支持其他自定义字段。
*   **方法**:
    *   `treeToList<T extends TreeNode>(tree: T[]): (T & { level: number })[]`: 将树结构转换为列表结构 (扁平化)。
    *   `findKeysByLevel<T extends TreeNode>(tree: T[], level: number): (string | number)[]`: 根据层级查找所有节点的 ID 列表。
    *   `buildTree<T extends TreeNode>(list: T[], idKey?: keyof T | 'id', pidKey?: keyof T | 'pid'): T[]`: 将扁平数组转换为树结构。
    *   `walk<T extends TreeNode>(tree: T[], callback: (node: T) => void): void`: 深度优先遍历树节点。
    *   `findByKey<T extends TreeNode>(key: string | number, list: T[], keyName?: keyof T | 'id'): T | undefined`: 根据键值深度查找单个节点。
    *   `findByKeyList<T extends TreeNode>(treeData: T[], keyList: (string | number)[]): T[]`: 根据键值列表查找所有匹配的节点列表。
    *   `getSimpleList<T extends TreeNode>(treeNodeList: T[]): T[]`: 获得给定根节点列表下的所有节点 (扁平化列表)。
    *   `getKeyList<T extends TreeNode>(tree: T[], value: string | number): (string | number)[]`: 向上追溯，获取从根节点到指定值节点的完整 Key 路径。
*   **示例**:

    ```typescript
    import { TreeUtils } from '@/framework/utils';

    interface MyNode {
      id: number;
      pid: number | null;
      name: string;
      children?: MyNode[];
    }

    const flatList: MyNode[] = [
      { id: 1, pid: null, name: 'Root A' },
      { id: 2, pid: 1, name: 'Child A1' },
      { id: 3, pid: 1, name: 'Child A2' },
      { id: 4, pid: null, name: 'Root B' },
      { id: 5, pid: 2, name: 'Grandchild A1-1' },
    ];

    // 数组转树
    const tree = TreeUtils.buildTree(flatList);
    console.log('构建的树结构:', JSON.stringify(tree, null, 2));

    // 树转列表
    const listFromTree = TreeUtils.treeToList(tree);
    console.log('树转列表 (扁平化):', JSON.stringify(listFromTree, null, 2));

    // 查找节点
    const node = TreeUtils.findByKey(5, tree);
    console.log('查找节点 (ID 5):', node?.name);

    // 获取 Key 路径
    const keyPath = TreeUtils.getKeyList(tree, 5);
    console.log('获取 Key 路径 (ID 5):', keyPath);

    // 遍历
    console.log('树遍历:');
    TreeUtils.walk(tree, (nodeItem) => {
      console.log(`  - ${nodeItem.name} (ID: ${nodeItem.id})`);
    });
    ```

### `UrlUtils`

*   **工具类名称**: `UrlUtils`
*   **描述**: 提供了一系列对 URL 进行操作的静态方法，包括解析 URL 参数、获取路径名、参数对象与查询字符串转换、设置/删除 URL 参数以及路径拼接。
*   **类型定义**:
    *   `ParamsObject`: `Record<string, string | number | boolean | null | undefined>`, 用于 URL 参数的键值对对象。
*   **方法**:
    *   `getParams(url?: string | null): UrlUtils.ParamsObject`: 获取 URL 的参数对象。
    *   `getPathname(url: string | null | undefined): string | null`: 获取不带参数的基础 URL (pathname)。
    *   `paramsToSearch(params: UrlUtils.ParamsObject | null | undefined): string`: 将参数对象转换为 URL 中的查询字符串。
    *   `setParam(url: string, key: string, value: string | number | boolean | null | undefined): string`: 设置或删除 URL 中的一个参数。
    *   `replaceParam(url: string, key: string, value: string | number | boolean | null | undefined): string`: **已弃用**，请使用 `setParam` 代替。
    *   `join(path1: string, path2: string): string`: 连接两个路径片段。
*   **示例**:

    ```typescript
    import { UrlUtils } from '@/framework/utils';

    // 假设当前 URL 为 'http://localhost:8000/path/to/page?id=123&name=test'

    // 获取参数
    const params = UrlUtils.getParams();
    console.log('当前 URL 参数:', params);

    const specificUrlParams = UrlUtils.getParams('http://example.com/search?q=apple&sort=asc');
    console.log('指定 URL 参数:', specificUrlParams);

    // 获取路径名
    console.log('当前路径名:', UrlUtils.getPathname(location.href));
    console.log('指定 URL 路径名:', UrlUtils.getPathname('http://example.com/search?q=apple'));

    // 参数对象转查询字符串
    const newParams = { page: 1, limit: 10, category: 'books' };
    console.log('参数对象转查询字符串:', UrlUtils.paramsToSearch(newParams));

    // 设置/删除参数
    let currentUrl = 'http://localhost:8000/dashboard?user=Alice&active=true';
    let newUrl1 = UrlUtils.setParam(currentUrl, 'user', 'Bob');
    console.log('设置 user 参数:', newUrl1);

    let newUrl2 = UrlUtils.setParam(newUrl1, 'active', null); // 删除 active 参数
    console.log('删除 active 参数:', newUrl2);

    let newUrl3 = UrlUtils.setParam(newUrl2, 'newParam', 'value');
    console.log('添加新参数:', newUrl3);

    // 路径连接
    console.log('路径连接1:', UrlUtils.join('/api', '/users'));
    console.log('路径连接2:', UrlUtils.join('/base/', '/subpath'));
    ```

### `UuidUtils`

*   **工具类名称**: `UuidUtils`
*   **描述**: 基于 `uuid-random` 的工作进行封装，提供了一个生成符合 v4 标准的 UUID 的静态方法。
*   **方法**:
    *   `uuidV4(): string`: 生成一个符合 v4 标准的 UUID 字符串。
*   **示例**:

    ```typescript
    import { UuidUtils } from '@/framework/utils';

    const uuid = UuidUtils.uuidV4();
    console.log('生成的 UUID v4:', uuid);
    // 例如: "a1b2c3d4-e5f6-4a7b-8c9d-e0f1a2b3c4d5"
    ```

### `ValidateUtils`

*   **工具类名称**: `ValidateUtils`
*   **描述**: 提供了一系列用于验证输入数据的静态方法。
*   **方法**:
    *   `isEmail(emailStr: string): boolean`: 检查给定的字符串是否为有效的电子邮件地址。
*   **示例**:

    ```typescript
    import { ValidateUtils } from '@/framework/utils';

    console.log('有效邮箱:', ValidateUtils.isEmail('test@example.com')); // true
    console.log('无效邮箱 (缺少@):', ValidateUtils.isEmail('testexample.com')); // false
    console.log('无效邮箱 (格式错误):', ValidateUtils.isEmail('test@example')); // false
    console.log('无效邮箱 (空字符串):', ValidateUtils.isEmail('')); // false
    ```

### `DictUtils` (位于 `utils/system` 目录下)

*   **工具类名称**: `DictUtils`
*   **描述**: 提供了与系统字典数据交互的静态方法，包括根据字典类型获取字典列表、转换为 Ant Design `Select` 选项格式、获取字典项标签以及渲染带有颜色的字典标签。
*   **接口**:
    *   `DictItem`: `{ value: string | number; label: string; color?: string; }`, 定义了字典项的结构。
    *   `DictOption`: `{ value: string | number; label: string; }`, 定义了用于 `Select` 组件的选项结构。
*   **方法**:
    *   `dictList(code: string): DictItem[]`: 根据字典类型 `code` 返回字典数据列表。
    *   `dictOptions(typeCode: string): DictOption[]`: 将字典列表转换为 Ant Design `Select`/`Options` 格式。
    *   `dictLabel(typeCode: string, code: string | number): string | undefined`: 根据字典类型和字典项 `code` 获取对应的标签 (`label`)。
    *   `dictTag(typeCode: string, code: string | number): React.ReactElement | string | null`: 根据字典类型和字典项 `code` 获取对应的标签，并使用 Ant Design `Tag` 组件包装。
*   **示例**:

    ```tsx
    import React from 'react';
    import { DictUtils } from '@/framework/utils/system';
    import { Tag } from 'antd';

    // 假设 SysUtils.getDictInfo() 返回类似如下数据：
    // {
    //   'USER_STATUS': [
    //     { value: 'active', label: '启用', color: 'green' },
    //     { value: 'inactive', label: '禁用', color: 'red' },
    //     { value: 'pending', label: '待审核', color: 'blue' },
    //   ],
    //   'GENDER': [
    //     { value: 'M', label: '男' },
    //     { value: 'F', label: '女' },
    //   ]
    // }

    const MyComponent = () => {
      const userStatusList = DictUtils.dictList('userStatus');
      const genderOptions = DictUtils.dictOptions('GENDER');

      return (
        <div>
          <h3>用户状态列表:</h3>
          <ul>
            {userStatusList.map(item => (
              <li key={item.value}>
                {item.label} ({item.value}) {item.color && `颜色: ${item.color}`}
              </li>
            ))}
          </ul>

          <h3>性别选项 (用于 Select):</h3>
          {genderOptions.map(option => (
            <Tag key={option.value}>{option.label}</Tag>
          ))}

          <h3>获取字典标签:</h3>
          <p>用户状态 'active' 的标签: {DictUtils.dictLabel('userStatus', 'active')}</p>
          <p>用户状态 'unknown' 的标签: {DictUtils.dictLabel('userStatus', 'unknown') || '未知'}</p>

          <h3>渲染字典标签 (Tag):</h3>
          <p>用户 'active' 状态: {DictUtils.dictTag('userStatus', 'active')}</p>
          <p>用户 'inactive' 状态: {DictUtils.dictTag('userStatus', 'inactive')}</p>
          <p>性别 'M': {DictUtils.dictTag('GENDER', 'M')}</p>
          <p>不存在的字典项: {DictUtils.dictTag('GENDER', 'X') || '无'}</p>
        </div>
      );
    };

    export default MyComponent;
    ```

### `FormRegistryUtils` (位于 `utils/system` 目录下)

*   **工具类名称**: `FormRegistryUtils`
*   **描述**: 这是一个静态工具类，用于注册、获取和管理表单组件。
*   **方法**:
    *   `register(formKey: string, formComponent: FormComponent): void`: 注册表单组件。
    *   `get(formKey: string): FormComponent | null`: 获取已注册的表单组件。
    *   `has(formKey: string): boolean`: 检查表单是否已注册。
    *   `getAllKeys(): string[]`: 获取所有已注册的表单 Key。
*   **示例**:

    ```tsx
    import React from 'react';
    import { Form, Input, Button } from 'antd';
    import { FormRegistryUtils } from '@/framework/utils/system';

    // 定义一个示例表单组件
    const MyLoginFormComponent: React.FC = () => (
      <Form onFinish={() => console.log('Login form submitted')}>
        <Form.Item name="username" rules={[{ required: true, message: '请输入用户名!' }]}>
          <Input placeholder="用户名" />
        </Form.Item>
        <Form.Item name="password" rules={[{ required: true, message: '请输入密码!' }]}>
          <Input.Password placeholder="密码" />
        </Form.Item>
        <Form.Item>
          <Button type="primary" htmlType="submit">登录</Button>
        </Form.Item>
      </Form>
    );

    const MyRegisterFormComponent: React.FC = () => (
      <Form onFinish={() => console.log('Register form submitted')}>
        <Form.Item name="email" rules={[{ required: true, message: '请输入邮箱!' }]}>
          <Input placeholder="邮箱" />
        </Form.Item>
        <Form.Item>
          <Button type="primary" htmlType="submit">注册</Button>
        </Form.Item>
      </Form>
    );

    // 注册表单
    FormRegistryUtils.register('loginForm', MyLoginFormComponent);
    FormRegistryUtils.register('registerForm', MyRegisterFormComponent);

    // 使用示例
    const App: React.FC = () => {
      const LoginForm = FormRegistryUtils.get('loginForm');
      const RegisterForm = FormRegistryUtils.get('registerForm');
      const UnknownForm = FormRegistryUtils.get('unknownForm');

      return (
        <div>
          <h1>表单示例</h1>
          {LoginForm && (
            <>
              <h2>登录表单</h2>
              <LoginForm />
            </>
          )}
          {RegisterForm && (
            <>
              <h2>注册表单</h2>
              <RegisterForm />
            </>
          )}
          {UnknownForm === null && <p>未知表单未注册。</p>}

          <p>所有注册的表单 Key: {FormRegistryUtils.getAllKeys().join(', ')}</p>
          <p>loginForm 是否已注册: {String(FormRegistryUtils.has('loginForm'))}</p>
        </div>
      );
    };

    export default App;
    ```

### `HttpUtils` (位于 `utils/system` 目录下)

*   **工具类名称**: `HttpUtils`
*   **描述**: 封装了 `axios`，提供了一系列用于发送 HTTP 请求的静态方法，包括 GET、POST、POST FORM 和文件下载。它集成了请求前后的处理、响应解析、统一的错误处理和消息提示。
*   **方法**:
    *   `get(url: string, params?: any, options?: Partial<AxiosRequestConfig>): Promise<any>`: 发送 GET 请求。
    *   `post(url: string, data?: any, params?: any, options?: Partial<RequestOptions>): Promise<any>`: 发送 POST 请求 (`Content-Type: application/json`)。
    *   `postForm(url: string, data: any, options?: Partial<AxiosRequestConfig>): Promise<any>`: 发送 POST 表单请求 (`Content-Type: application/x-www-form-urlencoded`)。
    *   `downloadFile(url: string, data?: any, params?: any, method?: Method, options?: Partial<AxiosRequestConfig>): Promise<void>`: 下载文件。
*   **API 响应结构期望**: `{ success: boolean | null, message: string, data: any, code: number }`。
*   **示例**:

    ```typescript
    import { HttpUtils } from '@/framework/utils/system';

    // 假设后端接口返回 { success: true, message: "操作成功", data: {...} } 或 { success: false, message: "错误信息" }

    async function fetchUserData() {
      try {
        const userData = await HttpUtils.get('admin/users/profile', { userId: 123 });
        console.log('用户数据:', userData);
        // 成功消息 '操作成功' 会自动弹出
      } catch (error) {
        console.error('获取用户数据失败:', error);
        // 错误消息会自动弹出
      }
    }

    async function createNewPost() {
      try {
        const newPost = await HttpUtils.post('admin/posts/add', { title: '新帖子', content: '这是我的新帖子内容。' });
        console.log('创建帖子成功:', newPost);
      } catch (error) {
        console.error('创建帖子失败:', error);
      }
    }

    async function login(username, password) {
      try {
        const loginResult = await HttpUtils.postForm('admin/auth/login', { username, password });
        console.log('登录成功:', loginResult);
      } catch (error) {
        console.error('登录失败:', error);
      }
    }

    async function downloadReport() {
      try {
        await HttpUtils.downloadFile('admin/reports/download', { type: 'daily' }, null, 'POST');
        console.log('报表下载已开始。');
      } catch (error) {
        console.error('报表下载失败:', error);
      }
    }

    fetchUserData();
    createNewPost();
    // login('testuser', 'password123'); // 假设的登录调用
    // downloadReport();
    ```

### `PageUtils` (位于 `utils/system` 目录下)

*   **工具类名称**: `PageUtils`
*   **描述**: 提供了一系列页面相关的工具类，主要用于路由、URL 参数和页面跳转操作。它封装了 `umi` 的 `history` 对象，并提供了处理 URL 参数的方法。
*   **方法**:
    *   `redirectToLogin(): void`: 重定向到登录页，并携带当前页面的 URL 作为回跳参数 (`redirect`)。
    *   `currentParams(): Record<string, string | undefined>`: 获取当前 URL 的查询参数对象。
    *   `currentPathname(): string`: 获取当前 URL 的路径名 (不带查询参数和 Hash 符号 `#`)。
    *   `currentUrl(): string`: 获取 hash 后的完整路径 (带参数)。
    *   `currentPathnameLastPart(): string | undefined`: 获取当前路由的最后一个斜杠后面的单词。
    *   `open(path: string, label?: string): void`: 打开一个新页面，可选择添加一个 `_label` 参数。
    *   `openNoLayout(path: string): void`: 打开一个不带菜单、Header 等布局元素的页面。
    *   `currentLabel(): string | undefined`: 获取当前 URL 参数中名为 `_label` 的值。
    *   `closeCurrent(): void`: 发送一个自定义事件来关闭当前页面。
*   **弃用方法**: `currentLocationQuery()`, `currentPath()`。
*   **示例**:

    ```typescript
    import { PageUtils } from '@/framework/utils/system';
    import { history } from 'umi'; // 模拟 history 对象

    // 模拟 window.location.href 和 window.location.hash
    Object.defineProperty(window, 'location', {
      value: {
        href: 'http://localhost:8000/#/user/profile?id=123&name=test&_label=个人中心',
        hash: '#/user/profile?id=123&name=test&_label=个人中心',
      },
      writable: true,
    });

    // 模拟 history.push
    history.push = (path: string) => console.log('history.push called with:', path);


    console.log('当前 URL 参数:', PageUtils.currentParams());

    console.log('当前路径名:', PageUtils.currentPathname());

    console.log('当前完整 URL:', PageUtils.currentUrl());

    console.log('当前路径最后一部分:', PageUtils.currentPathnameLastPart());

    console.log('当前 Label:', PageUtils.currentLabel());

    // 示例: 跳转到登录页
    // PageUtils.redirectToLogin();

    // 示例: 打开一个新页面
    PageUtils.open('/product/detail', '产品详情');

    // 示例: 打开一个无布局页面
    PageUtils.openNoLayout('/settings/printer');
    ```

### `PermUtils` (位于 `utils/system` 目录下)

*   **工具类名称**: `PermUtils`
*   **描述**: 提供了一系列用于管理用户权限的静态方法，包括检查用户是否拥有特定权限、判断是否未授权等。
*   **接口**:
    *   `LoginInfo`: 定义了登录用户信息，包含 `permissions` (权限码数组)。
*   **方法**:
    *   `hasPermission(perm: string | null | undefined): boolean`: 检查用户是否拥有特定权限。支持超级权限 `*`。
    *   `isPermitted(p: string | null | undefined): boolean`: 是否授权，功能上与 `hasPermission` 相同。
    *   `notPermitted(p: string | null | undefined): boolean`: 是否没有权限。
*   **示例**:

    ```typescript
    import { PermUtils } from '@/framework/utils/system';

    // 假设 SysUtils.getLoginInfo() 返回一个模拟的 LoginInfo 对象
    // SysUtils.getLoginInfo = () => ({
    //   permissions: ['user:view', 'product:edit', '*'], // 模拟拥有超级权限和具体权限
    // });

    // 或者模拟用户只有特定权限
    // SysUtils.getLoginInfo = () => ({
    //   permissions: ['user:view'],
    // });

    // 或者模拟用户没有任何权限
    // SysUtils.getLoginInfo = () => ({
    //   permissions: [],
    // });


    console.log('拥有 "user:view" 权限:', PermUtils.hasPermission('user:view'));
    console.log('拥有 "product:add" 权限:', PermUtils.hasPermission('product:add'));
    console.log('拥有超级权限 "*":', PermUtils.hasPermission('*'));

    console.log('isPermitted "product:edit":', PermUtils.isPermitted('product:edit'));
    console.log('notPermitted "user:delete":', PermUtils.notPermitted('user:delete'));
    ```

### `SysUtils` (位于 `utils/system` 目录下)

*   **工具类名称**: `SysUtils`
*   **描述**: 提供了一系列用于管理系统相关信息（如站点信息、登录信息、字典信息）的静态方法。
*   **常量**: `SITE_INFO_KEY`, `LOGIN_INFO_KEY`, `DICT_INFO_KEY`。
*   **接口**: `SiteInfo`, `LoginInfo`, `DictInfo`。
*   **方法**:
    *   `getHeaders(): Record<string, string>`: 获取请求头信息。
    *   `setSiteInfo(data: SiteInfo): void`: 设置站点信息。
    *   `getSiteInfo(): SiteInfo`: 获取站点信息。
    *   `setLoginInfo(data: LoginInfo): void`: 设置登录信息。
    *   `getLoginInfo(): LoginInfo`: 获取登录信息。
    *   `setDictInfo(data: DictInfo): void`: 设置字典信息。
    *   `getDictInfo(): DictInfo`: 获取字典信息。
*   **示例**:

    ```typescript
    import { SysUtils } from '@/framework/utils/system';

    // 模拟 SiteInfo 和 LoginInfo 数据
    interface MockSiteInfo { name: string; version: string; }
    interface MockLoginInfo { token: string; userId: number; permissions: string[]; }
    interface MockDictInfo {
      USER_STATUS: { value: string; label: string; }[];
    }

    // 假设 StorageUtils 已经模拟好或者正常工作

    // 设置站点信息
    SysUtils.setSiteInfo({ name: 'Admin Dashboard', version: '1.0.0' } as MockSiteInfo);
    const siteInfo = SysUtils.getSiteInfo();
    console.log('站点信息:', siteInfo);

    // 设置登录信息
    SysUtils.setLoginInfo({ token: 'xyz123', userId: 1, permissions: ['user:view'] } as MockLoginInfo);
    const loginInfo = SysUtils.getLoginInfo();
    console.log('登录信息:', loginInfo);

    // 设置字典信息
    SysUtils.setDictInfo({
      USER_STATUS: [
        { value: 'active', label: '活跃' },
        { value: 'inactive', label: '非活跃' },
      ],
    } as MockDictInfo);
    const dictInfo = SysUtils.getDictInfo();
    console.log('字典信息:', dictInfo);

    console.log('获取请求头:', SysUtils.getHeaders());
    ```

### `ThemeUtils` (位于 `utils/system` 目录下)

*   **工具类名称**: `ThemeUtils`
*   **描述**: 提供了一系列主题相关的工具类，主要用于定义和获取主题颜色。
*   **属性**:
    *   `theme`: `readonly object`, 包含了预定义的主题颜色键值对。
*   **方法**:
    *   `getColor(key: keyof typeof ThemeUtils.theme): string | undefined`: 获取对应主题颜色键名的颜色值。
*   **示例**:

    ```typescript
    import { ThemeUtils } from '@/framework/utils/system';

    console.log('主要颜色:', ThemeUtils.getColor('primary-color'));
    console.log('背景颜色:', ThemeUtils.getColor('background-color'));
    console.log('hover 颜色:', ThemeUtils.getColor('primary-color-hover'));
    console.log('不存在的颜色键:', ThemeUtils.getColor('non-existent-color' as any));
    ```
