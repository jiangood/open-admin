import React from 'react';
import { Spin, message } from 'antd';
import { debounce } from 'lodash';
import { HttpUtils } from '../utils';

/**
 * 远程数据加载选择器基类
 *
 * 处理通用的数据加载、防抖搜索、竞态处理、错误提示逻辑。
 * 子类只需覆写 getLoadParams() 和 render()。
 */
class BaseRemoteSelect extends React.Component {
    constructor(props) {
        super(props);
        this.fetchIdRef = 0;
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
    getUrl() {
        return this.props.url;
    }

    /** 返回请求参数。searchText=undefined 表示初始加载 */
    getLoadParams(searchText) {
        return { searchText, selected: this.props.value };
    }

    /** 是否在挂载时自动加载数据 */
    shouldLoadOnMount() {
        return true;
    }

    // ========== 数据加载 ==========

    _loadData = async (searchText) => {
        const url = this.getUrl();
        const fetchId = ++this.fetchIdRef;

        this.setState({ loading: true });

        try {
            const data = await HttpUtils.get(url, this.getLoadParams(searchText));

            if (fetchId === this.fetchIdRef) {
                this.setState({ data: data || [] });
            }
        } catch (error) {
            console.error('远程加载失败:', error);
            message.error('加载失败，请重试');
            if (fetchId === this.fetchIdRef) {
                this.setState({ data: [] });
            }
        } finally {
            if (fetchId === this.fetchIdRef) {
                this.setState({ loading: false });
            }
        }
    };

    /**
     * 触发数据加载
     * - 传 searchText → 防抖后加载（用于搜索输入）
     * - 不传 → 立即加载（用于初始加载）
     */
    loadData = (searchText) => {
        if (searchText != null) {
            this.loadDataDebounce(searchText);
        } else {
            this._loadData();
        }
    };

    /** 搜索输入处理（供 showSearch.onSearch 使用） */
    handleSearch = (value) => {
        if (value.trim() === '') {
            this.loadData();
            return;
        }
        this.loadData(value.trim());
    };

    // ========== Helper 方法 ==========

    getShowSearch() {
        return {
            filterOption: false,
            onSearch: this.handleSearch,
        };
    }

    getNotFoundContent() {
        return this.state.loading ? <Spin size="small" /> : '数据为空';
    }
}

export default BaseRemoteSelect;
