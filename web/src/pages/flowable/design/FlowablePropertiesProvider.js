import assignGroup from './parts/AssigneeGroup'; // 自定义 Assignee 组的逻辑

export default function FlowablePropertiesProvider(eventBus, bpmnFactory, elementRegistry, translate) {

    // 核心方法：为特定元素创建属性组
    this.getGroups = function(element) {
        // 基础属性组 (Name, ID)
        const generalGroup = { /* ... */ };

        // 如果是 UserTask，则添加 Flowable 特有的 Assignee 组
        if (element.type === 'bpmn:UserTask') {
            return [
                generalGroup,
                assignGroup(element, bpmnFactory, translate) // Flowable 任务分配配置
            ];
        }

        return [ generalGroup ];
    };
}
// 注入依赖
FlowablePropertiesProvider.$inject = [
    'eventBus',
    'bpmnFactory',
    'elementRegistry',
    'translate'
];
