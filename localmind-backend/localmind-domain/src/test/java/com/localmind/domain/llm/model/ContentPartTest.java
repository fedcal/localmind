package com.localmind.domain.llm.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ContentPartTest {

    @Test
    void builder_shouldCreateContentPart() {
        // when
        ContentPart part = ContentPart.builder()
                .type(ContentPart.ContentType.IMAGE)
                .content("base64data")
                .mimeType("image/png")
                .build();

        // then
        assertThat(part.getType()).isEqualTo(ContentPart.ContentType.IMAGE);
        assertThat(part.getContent()).isEqualTo("base64data");
        assertThat(part.getMimeType()).isEqualTo("image/png");
    }

    @Test
    void noArgsConstructor_shouldCreateEmptyContentPart() {
        // when
        ContentPart part = new ContentPart();

        // then
        assertThat(part.getType()).isNull();
        assertThat(part.getContent()).isNull();
        assertThat(part.getMimeType()).isNull();
    }

    @Test
    void allArgsConstructor_shouldSetAllFields() {
        // when
        ContentPart part = new ContentPart(ContentPart.ContentType.AUDIO, "audiodata", "audio/mp3");

        // then
        assertThat(part.getType()).isEqualTo(ContentPart.ContentType.AUDIO);
        assertThat(part.getContent()).isEqualTo("audiodata");
        assertThat(part.getMimeType()).isEqualTo("audio/mp3");
    }

    @Test
    void contentType_shouldHaveExpectedValues() {
        assertThat(ContentPart.ContentType.values()).containsExactly(
                ContentPart.ContentType.TEXT,
                ContentPart.ContentType.IMAGE,
                ContentPart.ContentType.AUDIO
        );
    }

    @Test
    void textType_shouldBeCreatable() {
        ContentPart part = ContentPart.builder()
                .type(ContentPart.ContentType.TEXT)
                .content("Hello world")
                .build();

        assertThat(part.getType()).isEqualTo(ContentPart.ContentType.TEXT);
        assertThat(part.getContent()).isEqualTo("Hello world");
        assertThat(part.getMimeType()).isNull();
    }

    @Test
    void equals_shouldWorkCorrectly() {
        ContentPart part1 = ContentPart.builder()
                .type(ContentPart.ContentType.IMAGE)
                .content("data1")
                .mimeType("image/jpeg")
                .build();

        ContentPart part2 = ContentPart.builder()
                .type(ContentPart.ContentType.IMAGE)
                .content("data1")
                .mimeType("image/jpeg")
                .build();

        assertThat(part1).isEqualTo(part2);
    }

    @Test
    void setters_shouldUpdateFields() {
        ContentPart part = new ContentPart();
        part.setType(ContentPart.ContentType.IMAGE);
        part.setContent("newdata");
        part.setMimeType("image/webp");

        assertThat(part.getType()).isEqualTo(ContentPart.ContentType.IMAGE);
        assertThat(part.getContent()).isEqualTo("newdata");
        assertThat(part.getMimeType()).isEqualTo("image/webp");
    }
}
