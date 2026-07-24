import React from "react";
import {HttpUtils, Page} from "../../framework";

export default class extends React.Component {

    state = {
        article: null,
        loading: true,
    }

    componentDidMount() {
        const code = this.props.match.params.code
        if (code) {
            HttpUtils.get('admin/article/getByCode', {code}).then(rs => {
                this.setState({article: rs, loading: false})
            }).catch(() => {
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

        return <Page title={article.title}>
            <div dangerouslySetInnerHTML={{__html: article.content}}/>
        </Page>
    }
}
