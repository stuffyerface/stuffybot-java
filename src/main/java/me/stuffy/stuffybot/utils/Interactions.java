package me.stuffy.stuffybot.utils;

import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

public class Interactions {
    public static void resolveSlashInteraction(SlashCommandInteractionEvent interaction) {
        // Get the hook, defer the reply
        // Call getResponse with the action and args
        // Edit the hook with the response
        // Log the interaction
    }

    public static void resolveButtonInteraction(ButtonInteractionEvent interaction) {
        // Get the button id
        // check if the user is the owner, if not return
        // Call getResponse with the action and args
        // Edit the hook with the response

        String interactionUser = interaction.getUser().getId();
        String interactionId = interaction.getComponentId();

        String[] split = interactionId.split(":");
        String buttonType = split[0];
        String ownerId = split[1];
        String args = split[2];

        if(!interactionUser.equals(ownerId)) {
            System.out.println("User " + interactionUser + " tried to interact with " + ownerId + "'s button.");
            return;
        }

        MessageEmbed getResponse = getResponse(buttonType, args);
        interaction.getHook().editOriginalEmbeds(getResponse).queue();
    }

    private static MessageEmbed getResponse(String action, String args) {
        // Call the appropriate response class
        // Return the response

        String actionClass = action.split("_")[0];
//        switch (actionClass) {
//            case "pit":
//                return PitResponses.getResponse(action, args);
//            default:
//                return null;
//        }
        return null;
    }
}
