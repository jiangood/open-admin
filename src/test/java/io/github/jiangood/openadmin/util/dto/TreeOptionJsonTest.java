package io.github.jiangood.openadmin.util.dto;

import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TreeOptionJsonTest {

    private final ObjectMapper om = new ObjectMapper();

    @Test
    public void serializedJsonContainsValueEqualToKey() throws Exception {
        TreeOption node = new TreeOption("总部", "org-1", null);
        String json = om.writeValueAsString(node);
        System.out.println("JSON = " + json);
        assertTrue(json.contains("\"value\":\"org-1\""));
    }

    @Test
    public void setKeySyncsValue() throws Exception {
        TreeOption node = new TreeOption();
        node.setKey("org-2");
        String json = om.writeValueAsString(node);
        assertTrue(json.contains("\"value\":\"org-2\""));
        assertTrue(node.getValue().equals("org-2"));
    }

    @Test
    public void setValueSyncsKey() {
        TreeOption node = new TreeOption();
        node.setValue("org-3");
        assertTrue(node.getKey().equals("org-3"));
    }

    @Test
    public void keyNullOmitsValueDueToNonNull() throws Exception {
        TreeOption node = new TreeOption();
        String json = om.writeValueAsString(node);
        assertFalse(json.contains("\"value\""));
    }
}
