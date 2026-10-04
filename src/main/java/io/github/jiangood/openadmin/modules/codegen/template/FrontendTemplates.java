package io.github.jiangood.openadmin.modules.codegen.template;

import io.github.jiangood.openadmin.modules.codegen.dto.EntityMetaVO;
import io.github.jiangood.openadmin.modules.codegen.dto.FieldMetaVO;
import io.github.jiangood.openadmin.util.dto.Option;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * 前端 CRUD 页面模板（React + Ant Design）。
 * <p>
 * 各代码片段按零缩进生成，再通过 {@link #indent(String, int)} 统一缩进，保证输出格式稳定。
 */
public final class FrontendTemplates {

    private static final List<String> IMAGE_HINTS = List.of(
            "image", "img", "avatar", "photo", "pic", "banner", "icon", "logo", "cover", "headimg");

    private FrontendTemplates() {
    }

    public static String page(EntityMetaVO meta, String module, String label, String frontendImport) {
        List<FieldMetaVO> fields = meta.getFields();

        Set<String> antd = new LinkedHashSet<>(List.of("Button", "Form"));
        Set<String> framework = new LinkedHashSet<>(List.of(
                "FormModal", "HttpClient", "Page", "PermActions", "ProTable"));

        StringBuilder constants = new StringBuilder();
        boolean hasDictEnum = false;
        for (FieldMetaVO field : fields) {
            if (field.isFile()) {
                if (field.isHtml()) {
                    framework.add("FieldEditor");
                } else if (isImageField(field)) {
                    framework.add("FieldUploadImage");
                } else {
                    framework.add("FieldUploadFile");
                }
                continue;
            }
            switch (field.getCategory()) {
                case "BOOLEAN" -> {
                    framework.add("FieldBoolean");
                    antd.add("Select");
                }
                case "DATE", "DATETIME" -> framework.add("FieldDate");
                case "NUMBER" -> antd.add("InputNumber");
                case "TEXT" -> antd.add("Input");
                case "ENUM" -> {
                    if (field.getDictCode() != null) {
                        hasDictEnum = true;
                        framework.add("FieldDictSelect");
                    } else {
                        antd.add("Select");
                        constants.append(enumConstant(field)).append('\n');
                    }
                }
                default -> antd.add("Input");
            }
        }
        if (hasDictEnum) {
            framework.add("DictUtils");
        }
        for (FieldMetaVO field : fields) {
            if (field.isFile() && !field.isHtml()) {
                if (isImageField(field)) {
                    framework.add("ViewImage");
                } else {
                    framework.add("ViewFileButton");
                }
            }
        }
        if (fields.stream().anyMatch(f -> !f.isFile() && "BOOLEAN".equals(f.getCategory()))) {
            framework.add("ViewBoolean");
        }

        StringBuilder columns = new StringBuilder();
        for (FieldMetaVO field : fields) {
            if (field.isFile() && field.isHtml()) {
                continue; // 富文本不展示在列表
            }
            if (!columns.isEmpty()) {
                columns.append('\n');
            }
            columns.append(column(field));
        }
        columns.append("\n{\n    title: '创建时间',\n    dataIndex: 'createTime',\n},\n");
        columns.append(operationColumn(module));
        StringBuilder searchItems = new StringBuilder();
        boolean hasStringSearch = fields.stream()
                .anyMatch(f -> f.isSearchable() && "STRING".equals(f.getCategory()));
        if (hasStringSearch) {
            searchItems.append("""
                    <Form.Item label='关键字' name='searchText'>
                        <Input placeholder='请输入关键字'/>
                    </Form.Item>
                    """.stripTrailing());
        }
        for (FieldMetaVO field : fields) {
            if (!field.isSearchable()) {
                continue;
            }
            String item = searchItem(field);
            if (item.isEmpty()) {
                continue;
            }
            if (!searchItems.isEmpty()) {
                searchItems.append('\n');
            }
            searchItems.append(item.stripTrailing());
        }

        StringBuilder formItems = new StringBuilder();
        for (FieldMetaVO field : fields) {
            if (!formItems.isEmpty()) {
                formItems.append('\n');
            }
            formItems.append(formItem(field).stripTrailing());
        }

        StringBuilder out = new StringBuilder();
        out.append(buildImports(antd, framework, frontendImport)).append('\n');
        if (!constants.isEmpty()) {
            out.append(constants).append('\n');
        }
        out.append("export default class extends React.Component {\n\n");
        out.append("    modalRef = React.createRef()\n");
        out.append("    tableRef = React.createRef()\n\n");
        out.append("    columns = [\n").append(indent(columns.toString().stripTrailing(), 8)).append("\n    ]\n\n");
        out.append("""
                    handleAdd = () => this.modalRef.current.open({})
                    handleEdit = record => this.modalRef.current.open({...record})
                    handleSubmit = async values => {
                        const url = values.id ? 'admin/%1$s/update' : 'admin/%1$s/create'
                        await HttpClient.post(url, values)
                        this.tableRef.current.reload()
                    }
                    handleDelete = record => {
                        HttpClient.post('admin/%1$s/delete', {id: record.id}, null).then(() => this.tableRef.current.reload())
                    }

                    render() {
                        return <Page>
                            <ProTable
                                actionRef={this.tableRef}
                                toolBarRender={() => ( // NOSONAR: AntD 渲染函数惯例
                                    <PermActions>
                                        <Button perm='%1$s:create' type='primary' icon={<PlusOutlined/>} onClick={this.handleAdd}>新增</Button>
                                    </PermActions>
                                )}
                                request={(params) => HttpClient.get('admin/%1$s/page', params)}
                                columns={this.columns}
                                searchFormRender={() => ( // NOSONAR: AntD 渲染函数惯例
                                    <>
                """.formatted(module));
        if (!searchItems.isEmpty()) {
            out.append(indent(searchItems.toString(), 24)).append('\n');
        }
        out.append("""
                                    </>
                                )}
                            />
                            <FormModal ref={this.modalRef} title='%s' onFinish={this.handleSubmit}>
                """.formatted(label));
        if (!formItems.isEmpty()) {
            out.append(indent(formItems.toString(), 16)).append('\n');
        }
        out.append("""
                            </FormModal>
                        </Page>
                    }
                }
                """);
        return out.toString();
    }

    private static String operationColumn(String module) {
        return """
                {
                    title: '操作',
                    dataIndex: 'option',
                    render: (_, record) => (
                        <PermActions
                            size='small'
                            actions={[
                                {label: '编辑', perm: '%s:update', onClick: () => this.handleEdit(record)},
                                {label: '删除', perm: '%s:delete', confirm: '确定删除？', onClick: () => this.handleDelete(record)},
                            ]}
                        />
                    ),
                },
                """.formatted(module, module);
    }

    private static String buildImports(Set<String> antd, Set<String> framework, String frontendImport) {
        return "import {PlusOutlined} from '@ant-design/icons'\n"
                + "import {" + String.join(", ", antd) + "} from 'antd'\n"
                + "import React from 'react'\n"
                + "import {" + String.join(", ", framework) + "} from '" + frontendImport + "';\n";
    }

    private static String enumConstant(FieldMetaVO field) {
        StringBuilder sb = new StringBuilder();
        sb.append("const ").append(field.getName()).append("Options = [\n");
        List<Option> options = field.getOptions() == null ? List.of() : field.getOptions();
        for (Option option : options) {
            sb.append("    {label: '").append(option.getLabel()).append("', value: '")
                    .append(option.getValue()).append("'},\n");
        }
        sb.append("]");
        return sb.toString();
    }

    private static String column(FieldMetaVO field) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("    title: '").append(field.getLabel()).append("',\n");
        sb.append("    dataIndex: '").append(field.getName()).append("',\n");
        if (field.isFile() && !field.isHtml()) {
            if (isImageField(field)) {
                sb.append("    width: 80,\n");
                sb.append("    render: (v) => <ViewImage value={v} size={48}/>,\n");
            } else {
                sb.append("    render: (v) => <ViewFileButton value={v}/>,\n");
            }
        } else if ("BOOLEAN".equals(field.getCategory())) {
            sb.append("    render: (v) => <ViewBoolean value={v}/>,\n");
        } else if ("ENUM".equals(field.getCategory())) {
            if (field.getDictCode() != null) {
                sb.append("    render: (v) => DictUtils.dictLabel('").append(field.getDictCode())
                        .append("', v) || v,\n");
            } else {
                sb.append("    render: (v) => ").append(field.getName())
                        .append("Options.find(o => o.value === v)?.label || v,\n");
            }
        }
        sb.append("},");
        return sb.toString();
    }

    private static String searchItem(FieldMetaVO field) {
        if ("ENUM".equals(field.getCategory())) {
            if (field.getDictCode() != null) {
                return """
                        <Form.Item label='%s' name='%s'>
                            <FieldDictSelect typeCode='%s'/>
                        </Form.Item>
                        """.formatted(field.getLabel(), field.getName(), field.getDictCode());
            }
            return """
                    <Form.Item label='%s' name='%s'>
                        <Select allowClear options={%sOptions}/>
                    </Form.Item>
                    """.formatted(field.getLabel(), field.getName(), field.getName());
        }
        if ("BOOLEAN".equals(field.getCategory())) {
            return """
                    <Form.Item label='%s' name='%s'>
                        <Select allowClear options={[{label: '是', value: true}, {label: '否', value: false}]}/>
                    </Form.Item>
                    """.formatted(field.getLabel(), field.getName());
        }
        return "";
    }

    private static String formItem(FieldMetaVO field) {
        String required = field.isRequired() ? " rules={[{required: true}]}" : "";
        return """
                <Form.Item label='%s' name='%s'%s>
                    %s
                </Form.Item>
                """.formatted(field.getLabel(), field.getName(), required, component(field));
    }

    private static String component(FieldMetaVO field) {
        if (field.isFile()) {
            if (field.isHtml()) {
                return "<FieldEditor/>";
            }
            if (isImageField(field)) {
                return "<FieldUploadImage maxCount={1}/>";
            }
            return "<FieldUploadFile maxCount={1}/>";
        }
        return switch (field.getCategory()) {
            case "BOOLEAN" -> "<FieldBoolean/>";
            case "DATE" -> "<FieldDate type='YYYY-MM-DD'/>";
            case "DATETIME" -> "<FieldDate type='YYYY-MM-DD HH:mm:ss'/>";
            case "NUMBER" -> "<InputNumber style={{width: '100%'}}/>";
            case "TEXT" -> "<Input.TextArea rows={4}/>";
            case "ENUM" -> field.getDictCode() != null
                    ? "<FieldDictSelect typeCode='" + field.getDictCode() + "'/>"
                    : "<Select options={" + field.getName() + "Options}/>";
            default -> "<Input/>";
        };
    }

    private static boolean isImageField(FieldMetaVO field) {
        String name = field.getName().toLowerCase(Locale.ROOT);
        for (String hint : IMAGE_HINTS) {
            if (name.contains(hint)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 为文本块每一非空行添加指定数量缩进。
     */
    private static String indent(String text, int spaces) {
        String prefix = " ".repeat(spaces);
        StringBuilder sb = new StringBuilder();
        String[] lines = text.split("\n", -1);
        for (int i = 0; i < lines.length; i++) {
            if (i > 0) {
                sb.append('\n');
            }
            if (!lines[i].isEmpty()) {
                sb.append(prefix);
            }
            sb.append(lines[i]);
        }
        return sb.toString();
    }
}
