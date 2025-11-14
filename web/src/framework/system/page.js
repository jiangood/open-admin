import {history} from "umi";
import {StrUtil, UrlUtil} from "@/framework";

export class PageUtil {

    static redirectToLogin(){
        let url = PageUtil.currentUrl();
        url =encodeURIComponent(url)
        history.push('/login?redirect='+ url)
    }

    /**
     * @deprecated
     * @returns {{}}
     */
    static currentLocationQuery() {
       return this.currentParams()
    }

    static currentParams(){
        let url = window.location.href


        let hasQuery = url.indexOf('?') > 0
        if (!hasQuery) {
            return {}
        }

        const search = url.substring(url.indexOf('?') + 1);
        const kvs = search.split('&')

        const rs = {}
        for (let kv of kvs) {
            const kvArr = kv.split('=')
            const k = kvArr[0];
            let v = kvArr[1];
            if(v){
                v = decodeURI(v)
            }
            rs[k] = v;
        }

        return rs
    }

    //"http://localhost:8000/#/login?id=1"
    //  返回不带参的路径
    static currentPathname() {
        let path = window.location.hash.substring(1);
        return StrUtil.subBefore(path, '?')
    }

    /**
     * @deprecated
     * @returns {{}}
     */
    static currentPath() {
        let path = window.location.hash.substring(1);
        return path
    }

    /**
     * hash后的路径
     * @returns {string}
     */
    static currentUrl() {
        let path = window.location.hash.substring(1);
        return path
    }

    /**
     *  当前路由的最后一个斜杠后面的单词， 通常用于基于路径的路由
     * @returns {string|null}
     */
    static currentPathnameLastPart() {
        const path = this.currentPathname()
        return StrUtil.subAfterLast(path, '/')
    }

    static open(path,label) {
        if(label) {
            path = UrlUtil.setParam(path,'_label', label)
        }
        history.push(path)
    }

    /**
     * 打开一个不带菜单，header的页面
     * @param path
     */
    static openNoLayout(path) {
       path = UrlUtil.setParam(path,'_noLayout', true)

        history.push(path)
    }


    static currentLabel(){
        return this.currentParams()['_label']
    }

    static closeCurrent(){
        const event = new CustomEvent('close-page-event', {
            detail: { url: PageUtil.currentUrl() }
        });
        document.dispatchEvent(event)
    }

}
