package com.localmind.domain.llm.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PullProgress {
    private String status;
    private String digest;
    private long total;
    private long completed;
    private boolean done;
    private boolean error;
    private String errorMessage;

    public int getPercentage() {
        if (total <= 0) return -1;
        return (int) ((completed * 100) / total);
    }
}
