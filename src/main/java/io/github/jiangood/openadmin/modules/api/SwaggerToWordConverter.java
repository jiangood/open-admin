package io.github.jiangood.openadmin.modules.api;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import io.github.jiangood.openadmin.lang.JsonTool;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblWidth;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STTblWidth;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

/**
 * 交付级 Swagger/OpenAPI 转 Word 工具
 */
public class SwaggerToWordConverter {
    private final String json;
    private OpenAPI openAPI;
    private List<String> perms;

    public SwaggerToWordConverter(String json, List<String> perms) {
        this.json = json.trim();
        this.perms = perms;
    }

    public File convert() throws IOException {
        this.openAPI = JsonTool.jsonToBean(json, OpenAPI.class);
        try (XWPFDocument doc = new XWPFDocument()) {
            // 1. 封面标题
            createTitle(doc, "系统接口调用技术文档");

            // 2. 鉴权说明（简单示例）
            addSectionTitle(doc, "1. 鉴权说明");
            XWPFParagraph authP = doc.createParagraph();
            authP.createRun().setText("本接口采用 Bearer Token 认证。调用时请在 Header 中携带：Authorization: Bearer {your_token}");

            // 3. 遍历接口
            addSectionTitle(doc, "2. 接口列表");
            int index = 1;
            for (Map.Entry<String, PathItem> entry : openAPI.getPaths().entrySet()) {
                String path = entry.getKey();
                PathItem pathItem = entry.getValue();
                Map<PathItem.HttpMethod, Operation> operations = pathItem.readOperationsMap();

                for (Map.Entry<PathItem.HttpMethod, Operation> opEntry : operations.entrySet()) {
                    renderOperation(doc, index++, path, opEntry.getKey().name(), opEntry.getValue());
                }
            }

            File tempFile = FileUtil.createTempFile(".docx", true);
            try (FileOutputStream os = new FileOutputStream(tempFile)) {
                doc.write(os);
            }
            return tempFile;
        }
    }

    private void renderOperation(XWPFDocument doc, int order, String path, String method, Operation op) {
        if(!perms.contains(op.getOperationId())){
            return;
        }
        // 接口标题
        XWPFParagraph head = doc.createParagraph();
        head.setSpacingBefore(400);
        XWPFRun headRun = head.createRun();
        headRun.setBold(true);
        headRun.setFontSize(14);
        headRun.setText(order + ". " + StrUtil.blankToDefault(op.getSummary(), "未命名接口"));

        // 基本信息表
        XWPFTable infoTable = createTable(doc, 2, 2);
        fillRow(infoTable.getRow(0), "接口路径", path);
        fillRow(infoTable.getRow(1), "请求方式", method.toUpperCase());

        // 请求参数 (Query/Path)
        if (op.getParameters() != null && !op.getParameters().isEmpty()) {
            addLabel(doc, "请求参数 (Parameters):");
            XWPFTable pTable = createTable(doc, 1, 5);
            fillHeader(pTable.getRow(0), "名称", "位置", "类型", "必填", "描述");
            for (Parameter p : op.getParameters()) {
                XWPFTableRow row = pTable.createRow();
                row.getCell(0).setText(p.getName());
                row.getCell(1).setText(p.getIn());
                row.getCell(2).setText(p.getSchema() != null ? p.getSchema().getType() : "string");
                row.getCell(3).setText(Boolean.TRUE.equals(p.getRequired()) ? "是" : "否");
                row.getCell(4).setText(StrUtil.blankToDefault(p.getDescription(), "-"));
            }
        }

        // 请求体 (Body)
        if (op.getRequestBody() != null) {
            addLabel(doc, "请求体 (Request Body):");
            renderSchemaToTable(doc, op.getRequestBody().getContent());
        }

        // 返回结果
        addLabel(doc, "返回结构 (Responses):");
        if (op.getResponses() != null) {
            for (Map.Entry<String, ApiResponse> respEntry : op.getResponses().entrySet()) {
                XWPFParagraph p = doc.createParagraph();
                XWPFRun r = p.createRun();
                r.setItalic(true);
                r.setText("HTTP " + respEntry.getKey() + ": " + respEntry.getValue().getDescription());
                renderSchemaToTable(doc, respEntry.getValue().getContent());
            }
        }
    }

    private void renderSchemaToTable(XWPFDocument doc, Content content) {
        if (content == null || content.isEmpty()) return;
        MediaType mediaType = content.values().iterator().next();
        Schema<?> schema = mediaType.getSchema();

        XWPFTable table = createTable(doc, 1, 4);
        fillHeader(table.getRow(0), "字段名", "类型", "说明", "示例/枚举");
        expandSchema(schema, table, 0);
    }

    private void expandSchema(Schema<?> schema, XWPFTable table, int depth) {
        if (schema == null) return;

        // 解析引用
        if (StrUtil.isNotBlank(schema.get$ref())) {
            String refName = StrUtil.subAfter(schema.get$ref(), "/", true);
            expandSchema(openAPI.getComponents().getSchemas().get(refName), table, depth);
            return;
        }

        // 处理对象
        if (schema.getProperties() != null) {
            schema.getProperties().forEach((name, prop) -> {
                XWPFTableRow row = table.createRow();
                String prefix = depth > 0 ? "　".repeat(depth) + "└ " : "";
                row.getCell(0).setText(prefix + name);
                row.getCell(1).setText(prop.getType());
                row.getCell(2).setText(StrUtil.blankToDefault(prop.getDescription(), "-"));

                // 枚举值或示例
                String extra = "";
                if (prop.getEnum() != null) extra = "枚举: " + prop.getEnum();
                else if (prop.getExample() != null) extra = "例: " + prop.getExample();
                row.getCell(3).setText(extra);

                // 递归
                if ("object".equals(prop.getType()) || StrUtil.isNotBlank(prop.get$ref())) {
                    expandSchema(prop, table, depth + 1);
                } else if ("array".equals(prop.getType()) && prop.getItems() != null) {
                    expandSchema(prop.getItems(), table, depth + 1);
                }
            });
        }
        // 处理数组根节点
        else if ("array".equals(schema.getType()) && schema.getItems() != null) {
            expandSchema(schema.getItems(), table, depth);
        }
    }

    // --- 样式辅助方法 ---

    private XWPFTable createTable(XWPFDocument doc, int rows, int cols) {
        XWPFTable table = doc.createTable(rows, cols);
        table.setWidth("100%");
        // 设置固定表格布局（防止内容撑爆）
        CTTblWidth width = table.getCTTbl().getTblPr().addNewTblW();
        width.setType(STTblWidth.DXA);
        width.setW(BigInteger.valueOf(9072)); // A4纸大致宽度
        return table;
    }

    private void fillHeader(XWPFTableRow row, String... titles) {
        for (int i = 0; i < titles.length; i++) {
            XWPFTableCell cell = row.getCell(i);
            cell.setColor("F2F2F2");
            XWPFRun r = cell.getParagraphs().get(0).createRun();
            r.setBold(true);
            r.setText(titles[i]);
        }
    }

    private void fillRow(XWPFTableRow row, String key, String value) {
        row.getCell(0).setColor("F2F2F2");
        row.getCell(0).setText(key);
        row.getCell(1).setText(value);
    }

    private void createTitle(XWPFDocument doc, String text) {
        XWPFParagraph p = doc.createParagraph();
        p.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun r = p.createRun();
        r.setBold(true);
        r.setFontSize(22);
        r.setText(text);
        doc.createParagraph();
    }

    private void addSectionTitle(XWPFDocument doc, String title) {
        XWPFParagraph p = doc.createParagraph();
        p.setSpacingBefore(200);
        XWPFRun r = p.createRun();
        r.setBold(true);
        r.setFontSize(16);
        r.setText(title);
    }

    private void addLabel(XWPFDocument doc, String text) {
        XWPFParagraph p = doc.createParagraph();
        p.setSpacingBefore(100);
        XWPFRun r = p.createRun();
        r.setBold(true);
        r.setText(text);
    }


}