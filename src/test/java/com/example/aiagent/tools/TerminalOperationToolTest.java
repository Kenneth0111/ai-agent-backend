package com.example.aiagent.tools;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TerminalOperationToolTest {

    private final TerminalOperationTool tool = new TerminalOperationTool();

    @Test
    public void testExecuteCommand() {
        String result = tool.executeCommand("echo hello");
        assertNotNull(result);
        assertTrue(result.contains("hello"));
    }

    @Test
    public void testInvalidCommand() {
        String result = tool.executeCommand("not_a_real_command_12345");
        assertNotNull(result);
        assertTrue(result.contains("Process exited with code:"));
    }
}
