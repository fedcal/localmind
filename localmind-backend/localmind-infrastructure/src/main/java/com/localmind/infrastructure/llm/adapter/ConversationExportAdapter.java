package com.localmind.infrastructure.llm.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.localmind.domain.llm.model.ChatMessage;
import com.localmind.domain.llm.model.ConversationContext;
import com.localmind.domain.llm.port.out.ConversationExportPort;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
public class ConversationExportAdapter implements ConversationExportPort {

    private static final int PDF_MAX_LINE_WIDTH = 80;
    private static final float PDF_MARGIN = 50f;
    private static final float PDF_FONT_SIZE_TITLE = 16f;
    private static final float PDF_FONT_SIZE_HEADER = 12f;
    private static final float PDF_FONT_SIZE_BODY = 10f;
    private static final float PDF_LINE_SPACING = 14f;
    private static final float PDF_SECTION_SPACING = 20f;

    private final ObjectMapper objectMapper;

    public ConversationExportAdapter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public byte[] toJson(ConversationContext conversation) {
        Map<String, Object> wrapper = buildExportWrapper(List.of(conversation));
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(wrapper);
        } catch (IOException e) {
            throw new IllegalStateException("Errore durante l'esportazione JSON della conversazione", e);
        }
    }

    @Override
    public byte[] toMarkdown(ConversationContext conversation) {
        StringBuilder sb = new StringBuilder();
        sb.append("# ").append(conversation.getTitle() != null ? conversation.getTitle() : "Conversazione senza titolo").append("\n\n");
        sb.append("**Exported from LocalMind** - ").append(formatDate(Instant.now())).append("\n\n");
        sb.append("---\n\n");

        if (conversation.getMessages() != null) {
            for (ChatMessage message : conversation.getMessages()) {
                String roleLabel = formatRoleLabel(message.getRole());
                sb.append("### ").append(roleLabel).append("\n\n");
                sb.append(message.getContent() != null ? message.getContent() : "").append("\n\n");
            }
        }

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public byte[] toPdf(ConversationContext conversation) {
        try (PDDocument document = new PDDocument()) {
            PDType1Font fontBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDType1Font fontRegular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

            List<PDPageContentStream> openStreams = new ArrayList<>();
            float[] currentY = {0f};
            PDPageContentStream[] currentStream = {null};

            currentStream[0] = addNewPage(document, openStreams, currentY);

            // Titolo
            String title = conversation.getTitle() != null ? conversation.getTitle() : "Conversazione senza titolo";
            writeText(currentStream, currentY, document, openStreams, title, fontBold, PDF_FONT_SIZE_TITLE);
            currentY[0] -= PDF_SECTION_SPACING;

            // Data esportazione
            String exportDate = "Esportato da LocalMind - " + formatDate(Instant.now());
            writeText(currentStream, currentY, document, openStreams, exportDate, fontRegular, PDF_FONT_SIZE_BODY);
            currentY[0] -= PDF_SECTION_SPACING;

            // Messaggi
            if (conversation.getMessages() != null) {
                for (ChatMessage message : conversation.getMessages()) {
                    // Ruolo come intestazione
                    String roleLabel = formatRoleLabel(message.getRole());
                    currentY[0] -= PDF_SECTION_SPACING / 2;
                    writeText(currentStream, currentY, document, openStreams, roleLabel, fontBold, PDF_FONT_SIZE_HEADER);
                    currentY[0] -= PDF_LINE_SPACING / 2;

                    // Contenuto del messaggio
                    String content = message.getContent() != null ? message.getContent() : "";
                    List<String> lines = wrapText(content, PDF_MAX_LINE_WIDTH);
                    for (String line : lines) {
                        writeText(currentStream, currentY, document, openStreams, line, fontRegular, PDF_FONT_SIZE_BODY);
                    }
                }
            }

            // Chiudi l'ultimo stream aperto
            for (PDPageContentStream stream : openStreams) {
                stream.endText();
                stream.close();
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            return baos.toByteArray();

        } catch (IOException e) {
            throw new IllegalStateException("Errore durante l'esportazione PDF della conversazione", e);
        }
    }

    @Override
    public byte[] multipleToJson(List<ConversationContext> conversations) {
        Map<String, Object> wrapper = buildExportWrapper(conversations);
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(wrapper);
        } catch (IOException e) {
            throw new IllegalStateException("Errore durante l'esportazione JSON di conversazioni multiple", e);
        }
    }

    private Map<String, Object> buildExportWrapper(List<ConversationContext> conversations) {
        List<Map<String, Object>> conversationList = new ArrayList<>();

        for (ConversationContext conv : conversations) {
            Map<String, Object> convMap = new LinkedHashMap<>();
            convMap.put("title", conv.getTitle());
            convMap.put("systemPrompt", conv.getSystemPrompt());
            convMap.put("messages", mapMessages(conv.getMessages()));
            convMap.put("tags", conv.getTags());
            convMap.put("createdAt", conv.getCreatedAt() != null ? conv.getCreatedAt().toString() : null);
            convMap.put("updatedAt", conv.getUpdatedAt() != null ? conv.getUpdatedAt().toString() : null);
            conversationList.add(convMap);
        }

        Map<String, Object> wrapper = new LinkedHashMap<>();
        wrapper.put("exportVersion", "1.0");
        wrapper.put("exportedAt", Instant.now().toString());
        wrapper.put("source", "LocalMind");
        wrapper.put("conversations", conversationList);

        return wrapper;
    }

    private List<Map<String, Object>> mapMessages(List<ChatMessage> messages) {
        if (messages == null) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (ChatMessage msg : messages) {
            Map<String, Object> msgMap = new LinkedHashMap<>();
            msgMap.put("role", msg.getRole().name());
            msgMap.put("content", msg.getContent());
            msgMap.put("metadata", msg.getMetadata());
            result.add(msgMap);
        }
        return result;
    }

    private String formatRoleLabel(ChatMessage.Role role) {
        if (role == null) {
            return "Unknown";
        }
        return switch (role) {
            case USER -> "User";
            case ASSISTANT -> "Assistant";
            case SYSTEM -> "System";
            case TOOL -> "Tool";
        };
    }

    private String formatDate(Instant instant) {
        return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                .withZone(ZoneId.systemDefault())
                .format(instant);
    }

    private List<String> wrapText(String text, int maxWidth) {
        List<String> wrapped = new ArrayList<>();
        String[] paragraphs = text.split("\n");
        for (String paragraph : paragraphs) {
            if (paragraph.isEmpty()) {
                wrapped.add("");
                continue;
            }
            int start = 0;
            while (start < paragraph.length()) {
                int end = Math.min(start + maxWidth, paragraph.length());
                if (end < paragraph.length() && paragraph.charAt(end) != ' ') {
                    int lastSpace = paragraph.lastIndexOf(' ', end);
                    if (lastSpace > start) {
                        end = lastSpace;
                    }
                }
                wrapped.add(paragraph.substring(start, end).trim());
                start = end;
                if (start < paragraph.length() && paragraph.charAt(start) == ' ') {
                    start++;
                }
            }
        }
        return wrapped;
    }

    private PDPageContentStream addNewPage(PDDocument document, List<PDPageContentStream> openStreams,
                                            float[] currentY) throws IOException {
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);
        PDPageContentStream stream = new PDPageContentStream(document, page);
        stream.beginText();
        stream.setLeading(PDF_LINE_SPACING);
        currentY[0] = page.getMediaBox().getHeight() - PDF_MARGIN;
        stream.newLineAtOffset(PDF_MARGIN, currentY[0]);
        openStreams.add(stream);
        return stream;
    }

    private void writeText(PDPageContentStream[] currentStream, float[] currentY,
                            PDDocument document, List<PDPageContentStream> openStreams,
                            String text, PDType1Font font, float fontSize) throws IOException {
        if (currentY[0] < PDF_MARGIN + PDF_LINE_SPACING) {
            currentStream[0].endText();
            currentStream[0].close();
            openStreams.remove(currentStream[0]);
            currentStream[0] = addNewPage(document, openStreams, currentY);
        }
        currentStream[0].setFont(font, fontSize);
        currentStream[0].showText(sanitizeForPdf(text));
        currentStream[0].newLine();
        currentY[0] -= PDF_LINE_SPACING;
    }

    private String sanitizeForPdf(String text) {
        if (text == null) {
            return "";
        }
        // PDType1Font (Standard14Fonts) supporta solo caratteri Latin-1 (WinAnsiEncoding).
        // Sostituisci i caratteri non supportati con '?' per evitare IllegalArgumentException.
        StringBuilder sb = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c < 256 && c != '\n' && c != '\r' && c != '\t') {
                sb.append(c);
            } else if (c == '\t') {
                sb.append("    ");
            } else if (c >= 256) {
                sb.append('?');
            }
        }
        return sb.toString();
    }
}
