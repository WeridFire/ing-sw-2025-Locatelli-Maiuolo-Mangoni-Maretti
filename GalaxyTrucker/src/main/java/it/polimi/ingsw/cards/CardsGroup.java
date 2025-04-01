package it.polimi.ingsw.cards;

import it.polimi.ingsw.cards.exceptions.CardsGroupException;

import java.util.List;

public class CardsGroup {

    public List<Card> getGroupCards() {
        return groupCards;
    }

    /**
     * The list of cards present by this group.
     */
    private List<Card> groupCards;
    /**
     * The username of the player that is viewing the cards. Null if not being held.
     */
    private String heldBy = null;

    /**
     * If set to true, means it is a "covered" card group, and players should not be able to view it.
     */
    private boolean secret;

    /**
     * Instances a new card group, based on the containing cards and whether it is secret or not.
     * @param groupCards The cards in the group.
     * @param secret A value indicating if the group is secret or not.
     */
    public CardsGroup(List<Card> groupCards, boolean secret) {
        this.groupCards = groupCards;
        this.secret = secret;
    }

    /**
     * Shows the card group to a player.
     * @param playerName The player to show the card group to.
     * @throws CardsGroupException when a group is secret or is already held by someone.
     */
    public void showGroup(String playerName) throws CardsGroupException{
        if(secret || heldBy != null){
            throw new CardsGroupException("The group cannot be shown to another player.");
        }
        heldBy = playerName;
        //controller logic to send group to player
    }

    /**
     * Hides the group from a player, basically releasing it for others to view.
     * @throws CardsGroupException when a group is not being held by anyone or is secret.
     */
    public void hideGroup() throws CardsGroupException {
        if(heldBy == null || secret){
            throw new CardsGroupException("The group is not being held by anyone.");
        }
        heldBy = null;
    }

    public String getHeldBy() {
        return heldBy;
    }

    public boolean isSecret() {
        return secret;
    }
}
