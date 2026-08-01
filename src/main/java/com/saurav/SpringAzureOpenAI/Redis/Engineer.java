package com.saurav.SpringAzureOpenAI.Redis;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Engineer {

    private String id;
    private String title;
    private String category;
    private String content;
    private float[] embedding;

}
