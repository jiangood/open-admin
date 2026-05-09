import type { MutableRefObject } from 'react';

interface PageInfo {
    current: number;
    pageSize: number;
    total: number;
}

interface ActionType {
    pageInfo: PageInfo;
    reload: () => void;
    reset: () => void;
    setPageInfo: (info: Partial<PageInfo>) => void;
}

interface ActionProps {
    onCleanSelected: () => void;
    resetAll: () => void;
}

interface UserAction {
    pageInfo: PageInfo;
    reload: (resetPageIndex?: boolean) => Promise<void>;
    reloadAndRest: () => Promise<void>;
    reset: () => Promise<void>;
    clearSelected: () => void;
    setPageInfo: (rest: Partial<PageInfo>) => void;
}

/**
 * 获取用户的 action 信息
 */
export function useActionType(
    ref: MutableRefObject<UserAction | undefined>,
    action: ActionType,
    props: ActionProps,
): void {
    /** 这里生成action的映射，保证 action 总是使用的最新 只需要渲染一次即可 */
    const userAction: UserAction = {
        pageInfo: action.pageInfo,
        reload: async (resetPageIndex) => {
            if (resetPageIndex) {
                await action.setPageInfo({ current: 1 });
            }
            action?.reload();
        },
        reloadAndRest: async () => {
            props.onCleanSelected();
            await action.setPageInfo({ current: 1 });
            await action?.reload();
        },
        reset: async () => {
            await props.resetAll();
            await action?.reset?.();
            await action?.reload();
        },
        clearSelected: () => props.onCleanSelected(),
        setPageInfo: (rest) => action.setPageInfo(rest),
    };
    // eslint-disable-next-line no-param-reassign
    ref.current = userAction;
}
