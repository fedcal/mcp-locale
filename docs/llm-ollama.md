# Uso di Ollama e scelta LLM per i server MCP

Obiettivo: far girare tutti i server MCP con client che usano Ollama come backend locale, scegliendo modelli adatti al caso d'uso.

## Setup rapido Ollama
1. Installa Ollama dal sito ufficiale.
2. Avvia il servizio (`ollama serve`) o assicurati che sia in esecuzione.
3. Imposta eventuale host/porta personalizzata: `export OLLAMA_HOST=127.0.0.1:11434`.

## Modelli consigliati per MCP di questa repo
- **Weather**: modelli compatti con buona comprensione strutturata (es. `phi4`, `llama3.1:8b`). Bassa latenza, I/O testuale.
- **Eventi-amici**: modelli con buone capacità di ragionamento su testo e liste (es. `llama3.1:8b` o `mistral-nemo`). Serve seguire istruzioni e formattare ripartizioni.
- **Sviluppo/Code** (futuri MCP dev): modelli orientati al codice (es. `qwen2.5-coder:14b` o `deepseek-coder:6.7b`). Usali quando il toolchain è tecnico (lint, diff, prompt library).
- **Finanza/Spese**: modelli che gestiscono numeri e riepiloghi (es. `phi4` o `llama3.1:8b`), sufficiente contesto breve.

## Selezione modello lato client
In Codex/Claude (o altro client che punta a Ollama), specifica il modello da usare:
- Codex CLI: `codex chat --model ollama::<nome-modello>` (es. `ollama::phi4`).
- Config locale: imposta variabile `CODEX_DEFAULT_MODEL=ollama::<nome-modello>` o analogo per il tuo client.

## Suggerimenti operativi
- **Modello per dialogo**: scegli quello più vicino al dominio del task; puoi cambiare modello tra sessioni.
- **Temperature/brevity**: usa temperature basse (0.2–0.4) per istruzioni MCP, così il modello segue meglio i formati richiesti.
- **Contesto**: fornisci al modello il nome del server MCP e il tool da usare; i tool già includono descrizioni e JSON Schema.
- **Testing**: prova l'invocazione con prompt esempio per ogni MCP dopo aver cambiato modello, per verificare che la formattazione resti corretta.

## Note
- Ollama non richiede registrazione cloud; i modelli vanno prima scaricati (`ollama pull <nome>`).
- Mantieni STDOUT pulito per MCP; Ollama viene usato dal client, non dai server. I server MCP restano invariati.
