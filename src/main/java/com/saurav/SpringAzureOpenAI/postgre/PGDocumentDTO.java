package com.saurav.SpringAzureOpenAI.postgre;

import jakarta.persistence.Column;

public record PGDocumentDTO(String id,String title,String content,String category) {
}
