import {history} from 'umi';
import {EventBusUtils, HttpUtils, PageUtils} from "../utils";
/**
 * 将登录页面的公共方法提取出来，方便自定义登录页面时使用
 */
export class LoginPageUtils {


    static postLogin(values) {
        return new Promise((resolve, reject) => {
            console.log('开始登录')
            HttpUtils.post('/admin/auth/login', values).then(rs => {
                console.log('登录结果', rs)
                EventBusUtils.emit('loginSuccess')
                let redirect = LoginPageUtils.getRedirect();
                history.push(redirect)
                resolve(rs)
            }).catch(e => {
                console.log('登录错误', e)
                reject(e)
            })
        })

    }

    static getRedirect() {
        let redirect = PageUtils.currentParams()['redirect'];
        if (redirect) {
            console.log('重定向参数', redirect)
            redirect = decodeURIComponent(redirect)
        }else {
            redirect = '/'
        }
        return redirect;
    }
}