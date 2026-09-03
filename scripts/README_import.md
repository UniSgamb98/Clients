# Importazione del vecchio CRM

Lo script `generate_import_sql.py`, che si trova nella cartella `scripts`, legge
esclusivamente questi file predefiniti:

```text
../txt data/clients.txt
../txt data/tutte_le_note.txt
```

I percorsi sono relativi alla cartella `scripts`: nel progetto corrispondono a
`txt data/clients.txt` e `txt data/tutte_le_note.txt`. Lo script non cerca file
né in `scripts` né in `src/main/resources/importa`. Se uno o entrambi gli input
non esistono, termina senza generare gli SQL ed elenca chiaramente i file mancanti.

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

La struttura predefinita richiesta è quindi:

```text
Clients/
├── scripts/
│   ├── genera_import.bat
│   └── generate_import_sql.py
└── txt data/
    ├── clients.txt
    └── tutte_le_note.txt
```

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

## Esecuzione degli SQL su Derby

Dopo aver generato gli SQL, su Windows si può avviare:

```bat
scripts\esegui_import_derby.bat
```

Il BAT si collega per impostazione predefinita a
`jdbc:derby:I:/Clizr/Tommaso/Clients` con utente `APP`, password `pw`, e usa
`C:\Apache\db-derby-10.17.1.0-bin\lib\derbyrun.jar`. Verifica prima la presenza
di Java, Derby e di tutti gli SQL, quindi li esegue nel seguente ordine:

1. `import_operatori.sql`
2. `import_clienti.sql`
3. `import_contatti.sql`
4. `import_indirizzi.sql`
5. `import_telefoni.sql`
6. `import_email.sql`
7. `import_siti.sql`
8. `import_note_interazioni.sql`

L'ordine garantisce che operatori e clienti esistano prima dei record che li
referenziano. Il risultato completo di IJ viene scritto in
`import scripts\import_execution.log`; anche quando IJ restituisce codice zero,
il BAT controlla il log e segnala eventuali errori Derby.

La configurazione può essere cambiata senza modificare il BAT:

```bat
set "DERBY_LIB=D:\ApacheDerby\lib"
set "CLIENTS_DB_URL=jdbc:derby:D:\database\Clients"
set "CLIENTS_DB_USER=APP"
set "CLIENTS_DB_PASSWORD=pw"
scripts\esegui_import_derby.bat
```

Il BAT converte automaticamente gli eventuali `\` presenti in `CLIENTS_DB_URL`
in `/` prima di scrivere il file Java `.properties`. Questo passaggio è
necessario perché nei file `.properties` il backslash è un carattere di escape:
senza la conversione `I:\Clizr\Tommaso\Clients` verrebbe letto erroneamente come
`I:ClizrTommasoClients`.

Gli script dei clienti non sono idempotenti: eseguire l'import completo una
sola volta su un database con lo schema già creato e senza i clienti importati.
