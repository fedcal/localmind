package com.localmind.domain.llm.service;

import com.localmind.domain.common.exception.ResourceNotFoundException;
import com.localmind.domain.llm.model.ConversationContext;
import com.localmind.domain.llm.port.out.ConversationExportPort;
import com.localmind.domain.llm.port.out.ConversationImportPort;
import com.localmind.domain.llm.port.out.ConversationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConversationExportServiceTest {

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private ConversationExportPort conversationExportPort;

    @Mock
    private ConversationImportPort conversationImportPort;

    @InjectMocks
    private ConversationExportService service;

    @Test
    void exportToJson_success() {
        String id = "conv-1";
        ConversationContext conversation = ConversationContext.builder()
                .id(id)
                .title("Test Conversation")
                .build();
        byte[] expected = "{\"id\":\"conv-1\"}".getBytes(StandardCharsets.UTF_8);

        when(conversationRepository.findById(id)).thenReturn(Optional.of(conversation));
        when(conversationExportPort.toJson(conversation)).thenReturn(expected);

        byte[] result = service.exportToJson(id);

        assertThat(result).isEqualTo(expected);
        verify(conversationRepository).findById(id);
        verify(conversationExportPort).toJson(conversation);
    }

    @Test
    void exportToJson_notFound() {
        String id = "non-existent";

        when(conversationRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.exportToJson(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(id);

        verify(conversationRepository).findById(id);
        verifyNoInteractions(conversationExportPort);
    }

    @Test
    void exportToMarkdown_success() {
        String id = "conv-2";
        ConversationContext conversation = ConversationContext.builder()
                .id(id)
                .title("Markdown Conversation")
                .build();
        byte[] expected = "# Markdown Conversation".getBytes(StandardCharsets.UTF_8);

        when(conversationRepository.findById(id)).thenReturn(Optional.of(conversation));
        when(conversationExportPort.toMarkdown(conversation)).thenReturn(expected);

        byte[] result = service.exportToMarkdown(id);

        assertThat(result).isEqualTo(expected);
        verify(conversationRepository).findById(id);
        verify(conversationExportPort).toMarkdown(conversation);
    }

    @Test
    void exportToPdf_success() {
        String id = "conv-3";
        ConversationContext conversation = ConversationContext.builder()
                .id(id)
                .title("PDF Conversation")
                .build();
        byte[] expected = new byte[]{0x25, 0x50, 0x44, 0x46}; // %PDF

        when(conversationRepository.findById(id)).thenReturn(Optional.of(conversation));
        when(conversationExportPort.toPdf(conversation)).thenReturn(expected);

        byte[] result = service.exportToPdf(id);

        assertThat(result).isEqualTo(expected);
        verify(conversationRepository).findById(id);
        verify(conversationExportPort).toPdf(conversation);
    }

    @Test
    void exportMultipleToJson_success() {
        ConversationContext conv1 = ConversationContext.builder()
                .id("conv-1")
                .title("First")
                .build();
        ConversationContext conv2 = ConversationContext.builder()
                .id("conv-2")
                .title("Second")
                .build();
        byte[] expected = "[{\"id\":\"conv-1\"},{\"id\":\"conv-2\"}]".getBytes(StandardCharsets.UTF_8);

        when(conversationRepository.findById("conv-1")).thenReturn(Optional.of(conv1));
        when(conversationRepository.findById("conv-2")).thenReturn(Optional.of(conv2));
        when(conversationExportPort.multipleToJson(List.of(conv1, conv2))).thenReturn(expected);

        byte[] result = service.exportMultipleToJson(List.of("conv-1", "conv-2"));

        assertThat(result).isEqualTo(expected);
        verify(conversationRepository).findById("conv-1");
        verify(conversationRepository).findById("conv-2");
        verify(conversationExportPort).multipleToJson(List.of(conv1, conv2));
    }

    @Test
    void importFromJson_success() {
        InputStream inputStream = new ByteArrayInputStream("[]".getBytes(StandardCharsets.UTF_8));
        ConversationContext imported1 = ConversationContext.builder()
                .id("imp-1")
                .title("Imported 1")
                .build();
        ConversationContext imported2 = ConversationContext.builder()
                .id("imp-2")
                .title("Imported 2")
                .build();

        when(conversationImportPort.fromJson(inputStream)).thenReturn(List.of(imported1, imported2));
        when(conversationRepository.save(imported1)).thenReturn(imported1);
        when(conversationRepository.save(imported2)).thenReturn(imported2);

        List<ConversationContext> result = service.importFromJson(inputStream);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo("imp-1");
        assertThat(result.get(1).getId()).isEqualTo("imp-2");
        verify(conversationRepository).save(imported1);
        verify(conversationRepository).save(imported2);
    }

    @Test
    void importFromJson_emptyList() {
        InputStream inputStream = new ByteArrayInputStream("[]".getBytes(StandardCharsets.UTF_8));

        when(conversationImportPort.fromJson(inputStream)).thenReturn(Collections.emptyList());

        List<ConversationContext> result = service.importFromJson(inputStream);

        assertThat(result).isEmpty();
        verify(conversationRepository, never()).save(any());
    }
}
