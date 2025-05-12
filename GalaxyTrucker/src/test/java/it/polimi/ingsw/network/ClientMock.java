package it.polimi.ingsw.network;

import it.polimi.ingsw.network.messages.ClientUpdate;
import it.polimi.ingsw.network.rmi.RmiClient;
import it.polimi.ingsw.util.Default;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ClientMock extends RmiClient {

    private GameClient gameClient;
    private final IServer rmiServerMock;

    public ClientMock() throws RemoteException {
        try {
            Registry registry = LocateRegistry.getRegistry(Default.HOST, Default.RMI_PORT);
            rmiServerMock = (IServer) registry.lookup(Default.RMI_SERVER_NAME);
        } catch (RemoteException | NotBoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void init(GameClient gameClient) {
        this.gameClient = gameClient;
    }

    @Override
    public IServer getServer() {
        return rmiServerMock;
    }

    @Override
    public void updateClient(ClientUpdate clientUpdate) {
        gameClient.updateClient(clientUpdate);
    }
}
