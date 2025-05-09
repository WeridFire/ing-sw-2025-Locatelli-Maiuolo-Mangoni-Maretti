package it.polimi.ingsw.view.gui;

import it.polimi.ingsw.controller.cp.ICommandsProcessor;
import it.polimi.ingsw.network.GameClient;
import it.polimi.ingsw.network.messages.ClientUpdate;
import it.polimi.ingsw.view.IView;

import java.util.Deque;

// TODO: create GUI view

public class GUIView implements IView {

    public GUIView(GameClient gameClient) {

    }

    @Override
    public void init() {
        System.out.println("GUI function: init");
        // TODO: implement
    }

    @Override
    public void onUpdate(ClientUpdate update) {
        System.out.println("GUI function: onUpdate");
        // TODO: implement
    }

    @Override
    public void run() {
        System.out.println("GUI function: run");
        // TODO: implement
    }

    @Override
    public void onVoid() {
        System.out.println("GUI function: onVoid");
        // TODO: implement
    }

    @Override
    public void onRefresh() {
        System.out.println("GUI function: onRefresh");
        // TODO: implement
    }

    @Override
    public void onPing() {
        System.out.println("GUI function: onPing");
        // TODO: implement
    }

    @Override
    public void onScreen(String screenName) {
        System.out.println("GUI function: onScreen -> " + screenName);
        // TODO: implement
    }

    @Override
    public void onHelp() {
        System.out.println("GUI function: onHelp");
        // TODO: implement
    }

    @Override
    public void onDebug() {
        System.out.println("GUI function: onDebug");
        // TODO: implement
    }

    @Override
    public void onCheat(String cheatName) {
        System.out.println("GUI function: onCheat -> " + cheatName);
        // TODO: implement
    }

    @Override
    public void showInfo(String title, String content) {
        System.out.println("GUI function: showInfo -> [" + title + "] >> " + content);
        // TODO: implement
    }

    @Override
    public void showWarning(String title, String content) {
        System.out.println("GUI function: showWarning -> [" + title + "] >> " + content);
        // TODO: implement
    }

    @Override
    public void showError(String title, String content) {
        System.out.println("GUI function: showError -> [" + title + "] >> " + content);
        // TODO: implement
    }

    @Override
    public Deque<ICommandsProcessor> getCommandsProcessors() {
        return null;
    }
}
