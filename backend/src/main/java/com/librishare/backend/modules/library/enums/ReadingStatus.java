package com.librishare.backend.modules.library.enums;

public enum ReadingStatus {
    WANT_TO_READ, // Lista de Desejos (Não tenho)
    TO_READ,      // Para Ler (Tenho na estante) <--- NOVO
    READING,      // Lendo
    READ          // Lido
}