# Layout Tab & PageRender Refactoring Design

Date: 2026-05-29

## Motivation

Refactor the admin layout's multi-tab system and page rendering engine to:
1. Convert class components to function components with hooks
2. Add KeepAlive (page state preservation on tab switch)
3. Implement tab reuse by full URL (pathname + search)
4. Simplify PageRender by removing APP_DATA_CACHE hack and class wrapper
5. Improve tab overflow management

## Architecture

```
TabPageRender (function component)
  │
  ├── useTabs() custom hook
  │     ├── state: { tabs[], activeKey, accessOrder[] }
  │     ├── addTab(url, label, component)
  │     ├── removeTab(url)
  │     ├── activateTab(url)
  │     └── refreshTab(url)
  │
  ├── Ant Design Tabs (tab bar only, type='editable-card')
  │     ├── onEdit → removeTab
  │     └── onChange → activateTab
  │
  └── KeepAlive container
        └── {tabs.map(tab => (
              <div key={tab.key} hidden={tab.key !== activeKey}>
                {tab.content}
              </div>
            ))}
```

## Detailed Design

### 1. PageRender Rewrite

**File: `web/src/layouts/PageRender.jsx`**

Remove the class component wrapper and APP_DATA_CACHE hack.

Current (to be replaced):
- Function component wrapper calling `useAppData()`
- Fallback to module-level `APP_DATA_CACHE` when appData is empty
- `_PageRender` inner class component with two render modes (default + passLocation)
- `passLocationRender()` looks up from `appData.routeComponents` map

New design:
- Pure function component
- `useAppData()` + `matchRoutes()` directly
- No APP_DATA_CACHE
- Single render path via `matchRoutes`
- Remove passLocation mode

**File: `web/src/layouts/PageRender.d.ts`**

Update type declaration to match new function component signature.

### 2. TabPageRender Rewrite

**File: `web/src/layouts/admin/TabPageRender.jsx`**

Convert from class component to function component.

**State (useTabs hook):**
```typescript
interface TabItem {
  key: string;      // pathname + search (full URL)
  label: string;
  content: ReactNode;
  closable: boolean;
}
```

**Reuse logic:**
- Tab key = `location.pathname + location.search`
- On URL change: if key exists → activate; else → create new tab

**KeepAlive:**
- All tabs remain mounted in the DOM
- Use `hidden` attribute (or `display: none`) on inactive tab containers
- Tab content is created once (on first open) and never unmounted until explicitly closed

**Overflow management:**
```javascript
const MAX_TABS = 20;
// When exceeding:
// 1. Always keep the first tab (home/dashboard)
// 2. Keep the currently active tab
// 3. Keep the last 3 most recently accessed tabs
// 4. Remove the first unprotected tab
```

**Double-click refresh:**
- Track last tab click time in a ref
- If two clicks within 300ms on same tab → refresh
- Refresh: set content to "刷新中...", then restore original in next tick

**Close-page-event:**
- `useEffect` to add/remove DOM event listener for 'close-page-event'

**Label resolution:**
1. `_label` URL parameter (set by `PageUtils.open()`)
2. `pathMenuMap[pathname].name` (from menu definition)
3. Fallback: `'临时'`

### 3. AdminLayout Integration

**File: `web/src/layouts/admin/index.jsx`**

Minimal change — TabPageRender's props remain the same:
```jsx
<TabPageRender pathMenuMap={this.state.pathMenuMap} />
```

### 4. TypeScript / Type Declarations

- Update `PageRender.d.ts` to reflect the function component signature
- Add JSDoc types for TabPageRender and useTabs

## Files Changed

| File | Change |
|------|--------|
| `web/src/layouts/PageRender.jsx` | Rewrite to pure function component, remove cache & class wrapper |
| `web/src/layouts/PageRender.d.ts` | Update type declaration |
| `web/src/layouts/admin/TabPageRender.jsx` | Rewrite to function component + useTabs hook + KeepAlive |
| `web/src/layouts/admin/index.jsx` | Minimal/no change expected |

## Non-Goals

- AdminLayout itself remains a class component (out of scope)
- No right-click context menu on tabs
- No drag-to-reorder tabs
- No pinned tabs feature
