import React from 'react';
import { Spin } from 'antd';
import { debounce } from 'lodash';
import { HttpClient } from '../../utils';

interface BaseRemoteSelectState {
    data: Record<string, unknown>[];
    loading: boolean;
}

export interface BaseRemoteSelectProps {
    url?: string;
    value?: unknown;
    debounceTime?: number;
    [key: string]: unknown;
}

/**
 * 远程数据加载选择器基类
 *
 * 处理通用的数据加载、防抖搜索、竞态处理、错误提示逻辑。
 * 子类只需覆写 getLoadParams() 和 render()。
 */
export class BaseRemoteSelect<P extends BaseRemoteSelectProps = BaseRemoteSelectProps> extends React.Component<P, BaseRemoteSelectState> { // NOSONAR: state/防抖字段构造器赋值
    private fetchIdRef: number = 0; // NOSONAR: 构造器中赋值
    private loadDataDebounce: ReturnType<typeof debounce>;

    static readonly defaultProps = {
        debounceTime: 300,
    };

    constructor(props: P) {
        super(props);
        this.state = {
            data: [],
            loading: false,
        };
        this.loadDataDebounce = debounce(this._loadData, props.debounceTime || 300);
    }

    componentDidMount() {
        if (this.shouldLoadOnMount()) {
            this.loadData();
        }
    }

    componentWillUnmount() {
        this.loadDataDebounce.cancel();
    }

    // ========== 子类可覆写 ==========

    /** 返回请求 URL */
    getUrl(): string | undefined {
        return this.props.url;
    }

    /** 返回请求参数。searchText=undefined 表示初始加载 */
    getLoadParams(searchText?: string): Record<string, unknown> {
        return { searchText, selected: this.props.value };
    }

    /** 是否在挂载时自动加载数据 */
    shouldLoadOnMount(): boolean {
        return true;
    }

    // ========== 数据加载 ==========

    private _loadData = (searchText?: string) => {
        const url = this.getUrl();
        const fetchId = ++this.fetchIdRef;

        this.setState({ loading: true });

        const done = () => {
            if (fetchId === this.fetchIdRef) {
                this.setState({ loading: false });
            }
        };

        HttpClient.get(url, this.getLoadParams(searchText), {toastError: false}).then((data) => {
            if (fetchId === this.fetchIdRef) {
                this.setState({ data: data || [] });
            }
            done();
        }).catch((error) => {
            console.warn('[BaseRemoteSelect] 加载失败:', error);
            if (fetchId === this.fetchIdRef) {
                this.setState({ data: [] });
            }
            done();
        });
    };

    /**
     * 触发数据加载
     * - 传 searchText → 防抖后加载（用于搜索输入）
     * - 不传 → 立即加载（用于初始加载）
     */
    loadData = (searchText?: string) => {
        if (searchText != null) {
            this.loadDataDebounce(searchText);
        } else {
            this._loadData();
        }
    };

    /** 搜索输入处理（供 showSearch.onSearch 使用） */
    handleSearch = (value: string) => {
        if (value.trim() === '') {
            this.loadData();
            return;
        }
        this.loadData(value.trim());
    };

    // ========== Helper 方法 ==========

    getShowSearch(): { filterOption: false; onSearch: (value: string) => void } { // NOSONAR: 由子类通过 this.getShowSearch() 调用
        return {
            filterOption: false,
            onSearch: this.handleSearch,
        };
    }

    getNotFoundContent(): React.ReactNode { // NOSONAR: 由子类通过 this.getNotFoundContent() 调用
        return this.state.loading ? <Spin size="small" /> : '数据为空';
    }
}
