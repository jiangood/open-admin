import {LazyLog, ScrollFollow} from "react-lazylog";
import React from "react";
import {SysUtil} from "@tmgg/tmgg-base";
/**
 * https://mozilla-frontend-infra.github.io/react-lazylog/
 */
export default class extends React.Component {

  render() {
    const url = this.props.url;
    const headers = SysUtil.getHeaders();


    return <ScrollFollow
      startFollowing={true}
      render={({follow, onScroll}) => (
        <LazyLog url={url}
                 height={500}
                 follow={follow}
                 fetchOptions={{credentials:'include', ...headers}}
                 onScroll={onScroll}/>
      )}
    />

  }
}
