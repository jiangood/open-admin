import React from "react";
import {HttpClient, Page, UrlUtils} from "../../framework";
import "./article.less";

export default class extends React.Component {

    state = {
        article: null,
        loading: true,
    }

    componentDidMount() {
        const code = this.props.params?.code
        if (code) {
            HttpClient.get('admin/article/getByCode', {code}, rs => {
                this.setState({article: rs, loading: false})
            }, () => {
                this.setState({loading: false})
            })
        } else {
            this.setState({loading: false})
        }
    }

    render() {
        const {article, loading} = this.state

        if (loading) {
            return <Page title="加载中...">
                <div>加载中...</div>
            </Page>
        }

        if (!article) {
            return <Page title="文章不存在">
                <div>文章不存在或已禁用</div>
            </Page>
        }

        return <Page>
            <div className="oa-article">
                <div className="oa-article-title">
                    <h1>{article.title}</h1>
                    <div className="oa-article-meta">
                        {article.createUserLabel && <span>发布人：{article.createUserLabel}</span>}
                        {article.createTime && <span>发布时间：{article.createTime}</span>}
                    </div>
                </div>
                {article.mainImage && (
                    <div style={{marginBottom: 16, textAlign: 'center'}}>
                        <img
                            src={UrlUtils.contextPath('/file/' + article.mainImage)}
                            style={{maxWidth: '100%', maxHeight: 400, borderRadius: 4}}
                            alt='主图'
                        />
                    </div>
                )}
                <div dangerouslySetInnerHTML={{__html: article.content}}/>
            </div>
        </Page>
    }
}
