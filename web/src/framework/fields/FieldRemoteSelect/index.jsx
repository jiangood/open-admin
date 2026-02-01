import React, { useState, useEffect, useCallback, useRef } from 'react';
import { Select, Spin, message } from 'antd';
import { debounce } from 'lodash';
import { HttpUtils } from "../../utils";

export const FieldRemoteSelect = ({
    url,
    debounceTime = 500,
    paramsProcessor,
    dataProcessor,
    cacheSize = 10,
    initialLoad = true,
    clearOnSearch = false,
    onError,
    onLoad,
    value,
    ...rest
}) => {
    const [options, setOptions] = useState([]);
    const [loading, setLoading] = useState(false);
    const [cache, setCache] = useState(new Map());
    const fetchIdRef = useRef(0);
    const searchHistoryRef = useRef([]);

    const loadData = useCallback(async (searchText = '') => {
        const fetchId = ++fetchIdRef.current;
        const cacheKey = searchText || '_initial_';

        // 检查缓存
        if (cache.has(cacheKey)) {
            const cachedData = cache.get(cacheKey);
            setOptions(cachedData || []);
            onLoad?.(cachedData);
            return;
        }

        setLoading(true);

        try {
            let params = { searchText, selected: value };
            if (paramsProcessor) {
                params = paramsProcessor(params);
            }

            const data = await HttpUtils.get(url, params);
            const processedData = dataProcessor ? dataProcessor(data) : data;

            if (fetchId === fetchIdRef.current) {
                setOptions(processedData || []);
                
                // 更新缓存
                setCache(prevCache => {
                    const newCache = new Map(prevCache);
                    newCache.set(cacheKey, processedData || []);
                    
                    // 限制缓存大小
                    if (newCache.size > cacheSize) {
                        const firstKey = newCache.keys().next().value;
                        newCache.delete(firstKey);
                    }
                    
                    return newCache;
                });

                onLoad?.(processedData);
            }
        } catch (error) {
            console.error('远程搜索失败:', error);
            message.error('搜索失败，请重试');
            setOptions([]);
            onError?.(error);
        } finally {
            if (fetchId === fetchIdRef.current) {
                setLoading(false);
            }
        }
    }, [url, value, paramsProcessor, dataProcessor, cache, cacheSize, onError, onLoad]);

    // 使用 useMemo 缓存 debounce 函数
    const debouncedLoadData = useCallback(
        debounce((value) => loadData(value), debounceTime),
        [loadData, debounceTime]
    );

    useEffect(() => {
        if (initialLoad) {
            loadData();
        }
    }, [initialLoad, loadData]);

    const handleSearch = useCallback((searchValue) => {
        const trimmedValue = searchValue.trim();
        
        // 记录搜索历史
        if (trimmedValue && !searchHistoryRef.current.includes(trimmedValue)) {
            searchHistoryRef.current = [trimmedValue, ...searchHistoryRef.current].slice(0, 5);
        }
        
        if (trimmedValue === '') {
            setOptions([]);
            return;
        }
        
        debouncedLoadData(trimmedValue);
    }, [debouncedLoadData]);

    return (
        <Select
            showSearch={{
                filterOption: false,
                onSearch: handleSearch,
            }}
            options={options}
            notFoundContent={loading ? <Spin size="small" /> : '数据为空'}
            style={{ width: '100%', minWidth: 200 }}
            allowClear
            loading={loading}
            {...rest}
        />
    );
};

FieldRemoteSelect.defaultProps = {
    placeholder: '请搜索选择'
};