package io.github.jiangood.openadmin.modules.system.service;

import io.github.jiangood.openadmin.modules.system.dto.SysFileFolderNode;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SysFileServiceFolderTreeTest {

    @Test
    void buildFolderTree_shouldAggregatePathAndCount() {
        List<SysFileFolderNode> tree = SysFileService.buildFolderTree(List.of(
                "public/202601/a.jpg",
                "public/202601/b.jpg",
                "public/img/202601/c.png",
                "private/202601/d.txt"
        ));

        assertEquals(2, tree.size());

        SysFileFolderNode publicNode = tree.get(0);
        assertEquals("public", publicNode.getKey());
        assertEquals("公共文件", publicNode.getTitle());
        assertEquals(3, publicNode.getCount());

        SysFileFolderNode publicMonth = publicNode.getChildren().get(0);
        assertEquals("public/202601", publicMonth.getKey());
        assertEquals("2026年01月", publicMonth.getTitle());
        assertEquals(2, publicMonth.getCount());

        SysFileFolderNode imageNode = publicNode.getChildren().get(1);
        assertEquals("public/img", imageNode.getKey());
        assertEquals("图片", imageNode.getTitle());
        assertEquals(1, imageNode.getCount());

        SysFileFolderNode imageMonth = imageNode.getChildren().get(0);
        assertEquals("public/img/202601", imageMonth.getKey());
        assertEquals(1, imageMonth.getCount());

        SysFileFolderNode privateNode = tree.get(1);
        assertEquals("private", privateNode.getKey());
        assertEquals("私有文件", privateNode.getTitle());
        assertEquals(1, privateNode.getCount());
    }

    @Test
    void buildFolderTree_shouldIgnoreBlankAndRootLevelFiles() {
        assertTrue(SysFileService.buildFolderTree(null).isEmpty());
        assertTrue(SysFileService.buildFolderTree(List.of()).isEmpty());
        assertTrue(SysFileService.buildFolderTree(List.of("", "a.jpg")).isEmpty());
    }

    @Test
    void buildFolderTree_shouldSupportCustomDirectories() {
        List<SysFileFolderNode> tree = SysFileService.buildFolderTree(List.of(
                "public/合同/2026/甲方.pdf"
        ));

        assertEquals(1, tree.size());
        SysFileFolderNode publicNode = tree.get(0);
        assertEquals(1, publicNode.getCount());
        assertEquals("合同", publicNode.getChildren().get(0).getTitle());
        assertEquals("2026", publicNode.getChildren().get(0).getChildren().get(0).getTitle());
    }
}
