# Importazione del vecchio CRM

Lo script `generate_import_sql.py` legge `clients.txt` e `tutte_le_note.txt` da
`src/main/resources/importa`. Se la cartella non è presente usa, per compatibilità,
i file nella cartella `scripts`.

```bash
python3 scripts/generate_import_sql.py
```

## Avvio portatile su Windows

Su Windows è possibile fare doppio clic su `scripts\genera_import.bat` oppure
richiamarlo da qualsiasi directory. Il BAT ricava la posizione del progetto
dalla propria posizione, quindi non dipende dalla directory corrente:

```bat
C:\> D:\strumenti\Clients\scripts\genera_import.bat
```

Il BAT prova prima il launcher `py -3` e poi `python`; restituisce inoltre lo
stesso codice di uscita del generatore. Deve rimanere nella stessa cartella di
`generate_import_sql.py`, ma l'intera cartella del progetto può essere spostata
liberamente.

Anche le opzioni del generatore possono essere passate dal BAT. Per usare input
e output completamente esterni al progetto:

```bat
genera_import.bat --clients "D:\migrazione\clients.txt" ^
  --notes "D:\migrazione\tutte_le_note.txt" ^
  --output "D:\migrazione\sql-generati"
```

I percorsi possono contenere spazi, purché racchiusi tra virgolette.

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
