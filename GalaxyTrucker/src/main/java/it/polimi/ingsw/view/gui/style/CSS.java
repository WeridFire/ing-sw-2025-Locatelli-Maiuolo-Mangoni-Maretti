package it.polimi.ingsw.view.gui.style;

public class CSS {
    public static String CLIENTMANAGER_STYLESHEET = """
        .root {
            -fx-background-color: #1a1c2c;
            -fx-font-family: 'Consolas', monospace;
        }
        .label {
            -fx-text-fill: #d0d0e0;
            -fx-font-size: 14px;
        }
        .header-label {
            -fx-font-size: 28px;
            -fx-text-fill: #ff8f00;
            -fx-effect: dropshadow(gaussian, rgba(255,143,0,0.5), 10, 0, 0, 0);
        }
        .button {
            -fx-background-color: transparent;
            -fx-text-fill: #4dd0e1;
            -fx-border-color: #4dd0e1;
            -fx-border-width: 2px;
            -fx-padding: 8 16;
            -fx-font-size: 14px;
            -fx-background-radius: 0;
            -fx-border-radius: 0;
        }
        .button:hover {
            -fx-background-color: #4dd0e1;
            -fx-text-fill: #1a1c2c;
        }
        .text-field {
            -fx-background-color: rgba(0,0,0,0.2);
            -fx-text-fill: #d0d0e0;
            -fx-border-color: #444;
            -fx-border-width: 0 0 1 0;
            -fx-prompt-text-fill: #888;
        }
        .check-box {
            -fx-text-fill: #d0d0e0;
        }
        .check-box .box {
            -fx-background-color: transparent;
            -fx-border-color: #4dd0e1;
            -fx-border-radius: 0;
        }
        .check-box:selected .mark {
            -fx-background-color: #4dd0e1;
        }
        .combo-box {
            -fx-background-color: transparent;
            -fx-border-color: #4dd0e1;
            -fx-border-width: 2px;
        }
        .combo-box .arrow, .combo-box .arrow-button {
            -fx-background-color: #4dd0e1;
        }
        .combo-box .list-cell {
            -fx-background-color: #1a1c2c;
            -fx-text-fill: #d0d0e0;
        }
        .combo-box-popup .list-view .list-cell:hover {
            -fx-background-color: #4dd0e1;
            -fx-text-fill: #1a1c2c;
        }
        .list-view {
            -fx-background-color: rgba(0,0,0,0.2);
            -fx-border-color: #444;
            -fx-border-width: 1px;
        }
        .list-view .list-cell {
            -fx-background-color: transparent;
            -fx-text-fill: #d0d0e0;
            -fx-padding: 5px;
        }
        .list-view .list-cell:filled:hover {
            -fx-background-color: #4dd0e1;
            -fx-text-fill: #1a1c2c;
        }
        .list-view .list-cell:filled:selected {
            -fx-background-color: #ff8f00;
            -fx-text-fill: #1a1c2c;
        }
        .toggle-button {
            -fx-background-color: transparent;
            -fx-text-fill: #4dd0e1;
            -fx-border-color: #4dd0e1;
            -fx-border-width: 2px;
            -fx-padding: 8 16;
            -fx-font-size: 14px;
            -fx-background-radius: 0;
            -fx-border-radius: 0;
        }
        .toggle-button:hover {
            -fx-background-color: #4dd0e1;
            -fx-text-fill: #1a1c2c;
        }
        .toggle-button:selected {
            -fx-background-color: #ff8f00;
            -fx-text-fill: #1a1c2c;
            -fx-border-color: #ff8f00;
        }
        """;
}
