// @ts-ignore
import React from "react";

declare type PageRenderProps = {

   /**
    * 路径 如 /flowable/task/form
    */
   pathname: string;
   /***
    * 搜索参数 如 /?id=1
    */
   search?:string;

   /***
    *   是否把location信息透传到真正页面
    */
   passLocation?: boolean
};

export class PageRender extends React.Component<PageRenderProps, any> {
}
