# STRUTTURA PER IL NETWORKING DEL PROGETTO
_questo è un prototipo, quindi potrebbe non essere finale. Però è qualcosa per iniziare._

## GameServer
GameServer è il punto di partenza del codice del server.
Questo GameServer appena avviato crea un GamesHandler, e runna su un thread a sé (ovvero quello main avviato dal codice)

Quando viene istanziato, vengono creati su 2 thread diversi l'RMI server ed il SocketServer. Questi due sono i
punti di accesso dei clients, in base al metodo di comunicazione che vogliono.

In GameServer viene usata una threadpool fixed di dimensione 2 per rinforzare questa cosa.

### RmiServer

Il GameServer istanzia il RMIServer, e lo esporta sulla porta desiderata. Nell'esportarlo però esporta la sua interfaccia,
ovvero la maschera con solo i metodi che dovrà vedere il client.

Il client potrà connettersi alla stessa porta ed accedere a questo oggetto remoto, e vedervi sopra i metodi dell'interfaccia.

Dopodiché si mette in ascolto (gestito già da RMI). Quando un client si connette viene aggiunto alla lista
dei client con un UUID.

### SocketServer
Il GameServer avvia il socket server. Questo apre la websocket desiderata, e si mette in ascolto
(dopo finisco)