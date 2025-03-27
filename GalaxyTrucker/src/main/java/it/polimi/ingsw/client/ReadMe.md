# STRUTTURA PER IL NETWORKING DEL PROGETTO
_questo è un prototipo, quindi potrebbe non essere finale. Però è qualcosa per iniziare._

## GameServer
GameServer è il punto di partenza del codice del server.
Questo GameServer appena avviato crea un GamesHandler, e runna su un thread a sé (ovvero quello main avviato dal codice)

Quando viene istanziato, vengono creati su 2 thread diversi l'RMI server ed il SocketServer. Questi due sono i
punti di accesso dei clients, in base al metodo di comunicazione che vogliono.

In GameServer viene usata una threadpool fixed di dimensione 2 per rinforzare questa cosa.

---

### RmiServer

Il GameServer istanzia il RMIServer, e lo esporta sulla porta desiderata. Nell'esportarlo però esporta la sua interfaccia,
ovvero la maschera con solo i metodi che dovrà vedere il client.

Il client potrà connettersi alla stessa porta ed accedere a questo oggetto remoto, e vedervi sopra i metodi dell'interfaccia.

Dopodiché si mette in ascolto (gestito già da RMI). Quando un client si connette viene aggiunto alla lista
dei client con un UUID.

### RmiClient

Il client (ancora da finire) in RMI viene avviato, va sulla porta e ip specificato e pulla l'oggetto remoto. L'oggetto
che vedrà sarà una VirualServer, ovvero interfaccia su cui potrà chiamare diversi metodi.

---

### SocketServer

Il GameServer avvia il socket server. Questo apre la websocket desiderata, e si mette in ascolto.
Il SocketServer aspetta ogni singola apertura di una websocket.

Appena la socket rileva una nuova connessione, la aggiunge alla pool degli utenti connessi tramite socket.
Istanzia poi un PlayerSocketHandler, ovvero effettivamente l'oggetto con cui mandare e ricevere messaggi.

#### PlayerSocketHandler

Questa classe è il rappresentante della comunicazione con il player sul server. Dovrà contenere metodi per inviare errori
e messaggi, e anche per processare comunicazioni in entrata.

Appena istanziato, si mette costantemente in ascolto di messaggi (con runVirtualView()).
Appena arriva un messaggio, questo deve essere parsato (quindi convertito in un oggetto digeribile) e poi
inviato nell' RMIJunction

#### RmiJunction
L'RMI junction si occupa di tradurre i messaggi in entrata su websocket in metodi chiamabili, rendendo effettivamente
simile la comunicazione. Purtroppo, dato che non ci è dato usare REFLECTION, non possiamo proceduralmente capire il metodo
da chiamare dal contenuto del messaggio. Ad esempio sarebbe bello avere il messaggio che manda "startGame" e il nome della classe,
e il programma cerca proceduralmente il metodo nella classe passata. Beh questo a meno che lo programmiamo noi (e vien su un bel burdel)
si farebbe con reflection, che non è permessa. Quindi dovremo fare invece un protocollo che in base al tipo del messaggio
e ai parametri, viene chiamato il metodo corretto su un nuovo thread, effettivamente rendendolo come se fosse RMI.

Importante tenere traccia di tutto questo sistema su un documento, così da sapere le keywords a che metodo mappano, e
i tipi necessari. Andrebbe fatto su figma.

Da qui ne deriva che dobbiamo inizare a sincronizzare tutti i metodi del model per far si
che gli accessi contemporanei siano possibili.

#### SocketMessage
Il socket message è un esempio di come potremmo fare il protocollo di comunicazione. Viene passato  questo oggetto (deserializzato in json o come classe),
e il contenuto delle stringe dell'oggetto sono a loro volta altri oggetti deserializzati (opotremmo anche rendere la lista di Object, e fare un solo step di deserializzazione, vedete voi)

