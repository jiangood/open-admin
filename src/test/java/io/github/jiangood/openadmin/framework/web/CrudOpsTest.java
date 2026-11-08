package io.github.jiangood.openadmin.framework.web;

import io.github.jiangood.openadmin.framework.data.BaseService;
import io.github.jiangood.openadmin.framework.data.specification.Spec;
import io.github.jiangood.openadmin.util.BusinessException;
import io.github.jiangood.openadmin.util.dto.AjaxResult;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Persistable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CrudOpsTest {

    // ========== requireUpdate ==========

    @Test
    void requireUpdateRejectsBlankId() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> CrudOps.requireUpdate("  ", List.of("name"), Optional.of("row"), "记录不存在"));
        assertEquals(CrudOps.MSG_ID_REQUIRED, ex.getMessage());
    }

    @Test
    void requireUpdateRejectsEmptyFields() {
        BusinessException nullFields = assertThrows(BusinessException.class,
                () -> CrudOps.requireUpdate("id", null, Optional.of("row"), "记录不存在"));
        assertEquals(CrudOps.MSG_NO_FIELDS, nullFields.getMessage());

        BusinessException emptyFields = assertThrows(BusinessException.class,
                () -> CrudOps.requireUpdate("id", List.of(), Optional.of("row"), "记录不存在"));
        assertEquals(CrudOps.MSG_NO_FIELDS, emptyFields.getMessage());

        BusinessException blankFields = assertThrows(BusinessException.class,
                () -> CrudOps.requireUpdate("id", List.of(" ", ""), Optional.of("row"), "记录不存在"));
        assertEquals(CrudOps.MSG_NO_FIELDS, blankFields.getMessage());
    }

    @Test
    void requireUpdateDoesNotQueryWhenIdMissing() {
        AtomicBoolean queried = new AtomicBoolean(false);
        assertThrows(BusinessException.class, () -> CrudOps.requireUpdate(null, List.of("name"), () -> {
            queried.set(true);
            return Optional.empty();
        }, "记录不存在"));
        assertFalse(queried.get());
    }

    @Test
    void requireUpdateRejectsMissingRecord() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> CrudOps.requireUpdate("id", List.of("name"), Optional.empty(), "客户不存在"));
        assertEquals("客户不存在", ex.getMessage());
    }

    @Test
    void requireUpdateReturnsExistingRecord() {
        String existing = CrudOps.requireUpdate("id", List.of("name"), Optional.of("row"), "记录不存在");
        assertEquals("row", existing);
    }

    // ========== requireDelete ==========

    @Test
    void requireDeleteRejectsMissingRecord() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> CrudOps.requireDelete(Optional.empty(), "记录不存在"));
        assertEquals(CrudOps.MSG_RECORD_NOT_FOUND, ex.getMessage());
    }

    @Test
    void requireDeleteReturnsExistingRecord() {
        String existing = CrudOps.requireDelete(Optional.of("row"), "记录不存在");
        assertEquals("row", existing);
    }

    // ========== 响应辅助 ==========

    @Test
    void infoReturnsDataOrBusinessError() {
        AjaxResult found = CrudOps.info(Optional.of("row"), "记录不存在");
        assertTrue(found.isSuccess());
        assertEquals("row", found.getData());

        AjaxResult missing = CrudOps.info(Optional.empty(), "记录不存在");
        assertFalse(missing.isSuccess());
        assertEquals("记录不存在", missing.getMessage());
    }

    @Test
    void successResponsesUseStandardMessages() {
        assertEquals("创建成功", CrudOps.created().getMessage());
        assertEquals("row", CrudOps.created("row").getData());
        assertEquals("更新成功", CrudOps.updated().getMessage());
        assertEquals("删除成功", CrudOps.deleted().getMessage());
    }

    @Test
    void pageAppliesCustomizer() {
        FakeService service = new FakeService();
        Pageable pageable = PageRequest.of(0, 10);
        AtomicBoolean applied = new AtomicBoolean(false);

        AjaxResult result = CrudOps.page(service, pageable, spec -> {
            applied.set(true);
            spec.eq("name", "n");
            return spec;
        });

        assertTrue(result.isSuccess());
        assertTrue(applied.get());
        assertSame(service.page, result.getData());
    }

    @Test
    void pageWithoutCustomizerUsesServiceSpec() {
        FakeService service = new FakeService();
        AjaxResult result = CrudOps.page(service, PageRequest.of(0, 10), null);

        assertTrue(result.isSuccess());
        assertSame(service.page, result.getData());
        assertSame(service.spec, service.lastSpec);
    }

    static class FakeService extends BaseService<FakeRow> {

        private final Spec<FakeRow> spec = Spec.of();
        private final Page<FakeRow> page;

        private Specification<FakeRow> lastSpec;

        FakeService() {
            this.page = new PageImpl<>(List.of(new FakeRow("1")), PageRequest.of(0, 10), 1);
        }

        @Override
        public Spec<FakeRow> spec() {
            return spec;
        }

        @Override
        public Page<FakeRow> findAll(Specification<FakeRow> spec, Pageable pageable) {
            this.lastSpec = spec;
            return page;
        }
    }

    static class FakeRow implements Persistable<String> {

        private final String id;

        FakeRow(String id) {
            this.id = id;
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public boolean isNew() {
            return id == null;
        }
    }
}
