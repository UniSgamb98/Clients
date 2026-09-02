# Importazione del vecchio CRM

Lo script `generate_import_sql.py` legge `clients.txt` e `tutte_le_note.txt` da
`src/main/resources/importa`. Se la cartella non è presente usa, per compatibilità,
i file nella cartella `scripts`.

```bash
python3 scripts/generate_import_sql.py
```

È possibile indicare percorsi diversi con `--clients`, `--notes` e `--output`.
Gli SQL vengono creati in `import scripts` e vanno eseguiti nell'ordine elencato
in `import_report.txt`, dopo la creazione dello schema e su un database vuoto.

Il generatore:

- converte i valori speciali (`?`, `??`, `???`, `BLANK`, `NULL`, `NULLO` e vuoto) in `NULL`;
- crea gli operatori mancanti prima dei clienti;
- crea una nota e un'interazione collegate per ogni `chiamata`, incluse quelle `cancelled="true"`;
- applica come stato finale l'ultimo `newInterest` datato, con `Interessamento` come fallback;
- collega `ProssimaChiamata` all'ultima chiamata XML o crea un'interazione sintetica quando non esistono chiamate XML;
- genera UUID deterministici, così uno stesso input produce sempre lo stesso SQL.

Il report segnala righe malformate, date e coinvolgimenti non validi, XML non
leggibili, documenti non collegati e conteggi degli statement generati.
