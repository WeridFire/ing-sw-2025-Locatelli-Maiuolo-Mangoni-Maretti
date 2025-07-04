# Progetto di Ingegneria del Software
## Valutazione 30L/30

## Componenti del gruppo
- Davide Locatelli ([@Dere-Wah](https://github.com/Dere-Wah))
- Manuel Maiuolo ([@BestPlayerMMIII](https://github.com/BestPlayerMMIII))
- Adriano Claudio Mangoni ([@AdriCman](https://github.com/AdriCman))
- Filippo Maretti ([@WeridFire](https://github.com/WeridFire))

## Funzionalità implementate

| Funzionalità                  | Stato |
|:------------------------------|:-----:|
| Regole **complete** del gioco | ✔️ |
| Interfaccia **TUI**           | ✔️ |
| Interfaccia **GUI**           | ✔️ |
| Comunicazione via **Socket**  | ✔️ |
| Comunicazione via **RMI**     | ✔️ |
| **Persistenza**               | ✔️ |
| **Resilienza**                | ✔️ |
| **Ripristino**                | ✔️ |
| Modalità **volo di prova**    | ✔️ |

## Struttura ed Esecuzione dei JAR

I JAR includono tutte le dipendenze (grazie a `maven-shade-plugin`)
e le risorse grafiche (da `src/main/resources/assets/`),
pronte all’uso anche fuori da IntelliJ.

I file `client.jar` e `server.jar` si trovano nella cartella `/target` dopo la build con Maven:

```bash
mvn clean
mvn package -DskipTests -Pclient -f pom.xml
mvn package -DskipTests -Pserver -f pom.xml
```

e sono riportati in [deliverables/final/jar](deliverables/final/jar).


### Avvio del Server
```bash
java -jar server.jar [--socket-port <porta>] [--rmi-port <porta>]
````

**Parametri opzionali:**

* `--socket-port`, `-sp`: imposta la porta per il server socket (default: 1234)
* `--rmi-port`, `-rmip`: imposta la porta per il server RMI (default: 1111)

**Esempio:**

```bash
java -jar server.jar -sp 1337 -rmip 1099
```

---

### Avvio del Client TUI

```bash
java -jar client.jar --tui [--socket-client|--rmi-client] [--host <ip>] [--port <porta>]
```

**Parametri richiesti:**

* `--tui`: avvia l’interfaccia testuale

**Parametri opzionali:**

* `--host`, `-h`: IP del server (default: localhost)
* `--port`, `-p`: porta del server (default: 1234 per Socket, dato che è default)
* `--socket-client`, `-sc`: connessione via Socket
* `--rmi-client`, `-rmic`: connessione via RMI

Nota: solo uno tra `--socket-client` e `--rmi-client` può essere richiesto (default: Socket).

**Esempio:**

```bash
java -jar client.jar --tui --socket-client -h 127.0.0.1 -p 1337
```

---

### Avvio del Client GUI

```bash
java -jar client.jar --gui
```

**Note:**

* `--gui`: avvia l’interfaccia grafica
* La scelta tra Socket e RMI, oltre a host e porta, sono richiesti direttamente nell'interfaccia.

---

## Test

* La suite di test automatizzati copre la maggior parte delle classi del `model` e della `network`.
* Eseguibili via Maven con:

```bash
mvn test
```

### Copertura
La copertura del modulo model si attesta intorno al 75%

Abbiamo adottato buone pratiche progettuali, come l'uso di checked exceptions
e validazioni preventive, che rendono molti blocchi catch formalmente necessari
ma logicamente irraggiungibili nei flussi corretti, anche in casi appositamente errati.
Di conseguenza, alcune righe risultano non coperte, pur essendo implementate
in modo sicuro e verificato.

Il controller è stato testato in modo selettivo, focalizzandoci sulle parti più critiche,
come raccomandato.

Abbiamo quindi privilegiato la qualità e la significatività dei test,
garantendo la stabilità del sistema.


## 📌 Note Finali

* È possibile salvare e riprendere partite interrotte, anche dopo una chiusura forzata del server.
* Il sistema è progettato per reggere disconnessioni temporanee e ripristini automatici.
* GUI e TUI possono essere usate contemporaneamente con lo stesso server.
