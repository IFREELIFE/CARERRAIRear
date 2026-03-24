package com.endcareerai.platform.service.impl;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class StudentServiceImplTest {

    @Test
    void mergeGraphAndAiContext_returnsCombinedWhenBothPresent() {
        String merged = StudentServiceImpl.mergeGraphAndAiContext("graph", "ai", "job");
        assertEquals("Neo4j-Graph[job]=graph || AI-Source[job]=ai", merged);
    }

    @Test
    void mergeGraphAndAiContext_prefersGraphWhenAiMissing() {
        String merged = StudentServiceImpl.mergeGraphAndAiContext("graph-only", null, "student");
        assertEquals("Neo4j-Graph[student]=graph-only", merged);
    }

    @Test
    void mergeGraphAndAiContext_returnsAiWhenGraphMissing() {
        String merged = StudentServiceImpl.mergeGraphAndAiContext(null, "ai-only", "job");
        assertEquals("AI-Source[job]=ai-only", merged);
    }

    @Test
    void mergeGraphAndAiContext_returnsNullWhenBothMissing() {
        assertNull(StudentServiceImpl.mergeGraphAndAiContext(null, null, "job"));
    }

    @Test
    void mergeGraphAndAiContext_ignoresEmptyOrBlankValues() {
        assertNull(StudentServiceImpl.mergeGraphAndAiContext("", "   ", "job"));
    }
}
