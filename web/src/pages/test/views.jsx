import {ViewText, ViewBoolean, ViewBooleanEnableDisable, ViewFile, ViewFileButton, ViewImage, ViewPassword, ViewApproveStatus, ViewProcessInstanceProgress, ViewProcessInstanceProgressButton} from "../../framework";

export function ViewTextTest() {
    return <ViewText value='你好'  />
}

export function ViewBooleanTest() {
    return <ViewBoolean value={true}  />
}

export function ViewBooleanEnableDisableTest() {
    return <ViewBooleanEnableDisable value={true}  />
}

export function ViewTextEllipsisTest() {
    return <ViewText value='这是一段很长的文本，用于测试省略显示功能' ellipsis={true} maxLength={10}  />
}

export function ViewFileTest() {
    return <ViewFile value='file123' height='200px'  />
}

export function ViewFileButtonTest() {
    return <ViewFileButton value='file123' height='200px'  />
}

export function ViewImageTest() {
    return <ViewImage value='https://via.placeholder.com/150'  />
}

export function ViewPasswordTest() {
    return <ViewPassword value='password123'  />
}

export function ViewApproveStatusTest() {
    return <ViewApproveStatus value='approved'  />
}

export function ViewProcessInstanceProgressTest() {
    return <ViewProcessInstanceProgress value='process123'  />
}

export function ViewProcessInstanceProgressButtonTest() {
    return <ViewProcessInstanceProgressButton value='process123'  />
}

export default function () {
    return <>
        <h1>View 组件测试</h1>
        <div style={{marginBottom: '20px'}}>
            <h2>ViewText</h2>
            <ViewTextTest/>
        </div>
        <div style={{marginBottom: '20px'}}>
            <h2>ViewBoolean</h2>
            <ViewBooleanTest/>
        </div>
        <div style={{marginBottom: '20px'}}>
            <h2>ViewBooleanEnableDisable</h2>
            <ViewBooleanEnableDisableTest/>
        </div>
        <div style={{marginBottom: '20px'}}>
            <h2>ViewText (with ellipsis)</h2>
            <ViewTextEllipsisTest/>
        </div>
        <div style={{marginBottom: '20px'}}>
            <h2>ViewFile</h2>
            <ViewFileTest/>
        </div>
        <div style={{marginBottom: '20px'}}>
            <h2>ViewFileButton</h2>
            <ViewFileButtonTest/>
        </div>
        <div style={{marginBottom: '20px'}}>
            <h2>ViewImage</h2>
            <ViewImageTest/>
        </div>
        <div style={{marginBottom: '20px'}}>
            <h2>ViewPassword</h2>
            <ViewPasswordTest/>
        </div>
        <div style={{marginBottom: '20px'}}>
            <h2>ViewApproveStatus</h2>
            <ViewApproveStatusTest/>
        </div>
        <div style={{marginBottom: '20px'}}>
            <h2>ViewProcessInstanceProgress</h2>
            <ViewProcessInstanceProgressTest/>
        </div>
        <div style={{marginBottom: '20px'}}>
            <h2>ViewProcessInstanceProgressButton</h2>
            <ViewProcessInstanceProgressButtonTest/>
        </div>
    </>
}