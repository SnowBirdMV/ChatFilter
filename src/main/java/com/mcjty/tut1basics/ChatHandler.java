package com.mcjty.tut1basics;

import com.google.gson.GsonBuilder;
import com.mcjty.tut1basics.client.data.FilterRule;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.*;
import net.minecraft.network.chat.contents.LiteralContents;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import com.google.gson.Gson;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Arrays;
import org.slf4j.Logger;

public class ChatHandler {
    private final static Logger LOGGER = LogUtils.getLogger();

    public static Map<String, String> adMap = null;
    public static Map<String, String> overlayMap = null;
    private static List<FilterRule> filterRules;
    private static final int MAX_MESSAGES = 100;
    private static final List<Component> messageLog = Collections.synchronizedList(new LinkedList<>());
    public static List<Component> getMessageLog() {
        return new ArrayList<>(messageLog); // return a copy to avoid concurrent modification issues
    }

    public ChatHandler() {

    }

    @SubscribeEvent
    public static void clientChatReceived(ClientChatReceivedEvent e) {
        try {
            logMessage(e.getMessage());
            handleChat(e);
        }
        catch (Exception ex){
            LOGGER.info("GOT AN ERROR:");
            LOGGER.info(ex.getMessage());
        }
    }

    private static void logMessage(Component message) {
        synchronized (messageLog) {
            if (messageLog.size() >= MAX_MESSAGES) {
                messageLog.remove(0);
            }
            messageLog.add(message);
        }
        LOGGER.info("Logged message component: " + message.getString());
    }

    private static void handleChat(ClientChatReceivedEvent e) {
        if (!Tutorial1Basics.config.isModEnabled()){
            return;
        }
        filterRules = Tutorial1Basics.config.getFilterRules();
        LOGGER.info("Chat Received!");
        LOGGER.info("Message was: " + e.getMessage().getString().replace("\n", "\\n"));
        LOGGER.info("Unicode Message: " + escapeNonAscii(e.getMessage().getString()));
        LOGGER.info(convertToTellraw(e.getMessage()));
        Gson gson = new GsonBuilder().serializeNulls().create();
        LOGGER.info(gson.toJson(e.getMessage().getVisualOrderText()));

        if (runFilterLists(e)){
            return;
        }

        if (filterVotingMessages(e)){
            return;
        }

        String originalMessage = e.getMessage().getString();
        Component newComponent = null;
        //Check for message type
        if (originalMessage.startsWith("[G] ")) {
            newComponent = handlePlayerMessage(e);
        }
        else if (handleIVoteMessage(e)) {
            return;
        }


        if (newComponent != null) {
            e.setMessage(newComponent);
        }
    }

    private static Boolean runFilterLists(ClientChatReceivedEvent e) {
        Boolean foundMatch = false;
        FilterRule matchingRule = null;
        String eventMessage = e.getMessage().getString();
        for(FilterRule rule : filterRules){
            if (rule.isRegex()){
                if (Pattern.compile(rule.getFilterText()).matcher(eventMessage).matches()){
                    matchingRule = rule;
                    break;
                }
            }
            else if (eventMessage.equals(rule.getFilterText())){
                matchingRule = rule;
                break;
            }
        }
        if (matchingRule != null) {
            if (matchingRule.isOverlay()) {
                Minecraft.getInstance().gui.setOverlayMessage(e.getMessage(), true);
                e.setCanceled(true);
                return true;
            }
            else{
                e.setCanceled(true);
                return true;
            }
        }
        return false;
    }

    private static boolean filterVotingMessages(ClientChatReceivedEvent e) {
        String message = e.getMessage().getString();
        Pattern pattern = Pattern.compile("\\[I-Pixelmon\\] (.*) Just Voted! They recieved a Voting Key and \\$50 Poke Dollars! Type: \\/VOTE");
        Matcher matcher = pattern.matcher(message);
        //find the voters name if this is a vote
        if (matcher.find()) {
            MutableComponent newMessage = MutableComponent.create(new LiteralContents(matcher.group(1)))
                    .setStyle(Style.EMPTY.withColor(TextColor.parseColor("light_purple")));
            newMessage.append(MutableComponent.create(new LiteralContents(" Just Voted!")).setStyle(Style.EMPTY.withColor(TextColor.parseColor("white"))));
            Minecraft.getInstance().gui.setOverlayMessage(newMessage, true);
            e.setCanceled(true);
            return true;
        }
        return false;
    }

    private static boolean handleIVoteMessage(ClientChatReceivedEvent e) {
        String originalMessage = e.getMessage().getString();
        if (originalMessage.contains("Players Voted in the last") && originalMessage.contains("Help reach our Voting Goal for a Pixelmon Giveaway")) {
            e.setCanceled(true);
            return true;
        }
        return false;
    }

    private static String convertToTellraw(Component targetComponent){
        String tellrawString = "/tellraw @p [";
        List<Component> messageComponents = targetComponent.toFlatList();
        for(Component component : messageComponents){
            Style componentStyle = component.getStyle();
            tellrawString += "{\"text\":\"" + escapeNonAscii(component.getString()) + "\",";
            if (componentStyle.isBold()) tellrawString += "\"bold\":true,";
            else if (!componentStyle.isBold()) tellrawString += "\"bold\":false,";
            if (componentStyle.isItalic()) tellrawString += "\"italic\":true,";
            else if (!componentStyle.isItalic()) tellrawString += "\"italic\":false,";
            if (componentStyle.isUnderlined()) tellrawString += "\"underlined\":true,";
            else if (!componentStyle.isUnderlined()) tellrawString += "\"underlined\":false,";
            if (componentStyle.isObfuscated()) tellrawString += "\"obfuscated\":true,";
            else if (!componentStyle.isObfuscated()) tellrawString += "\"obfuscated\":false,";
            if (componentStyle.isStrikethrough()) tellrawString += "\"strikethrough\":true,";
            else if (!componentStyle.isStrikethrough()) tellrawString += "\"strikethrough\":false,";
            if (componentStyle.getHoverEvent() != null) tellrawString += "\"hoverEvent\":" + componentStyle.getHoverEvent().serialize() + ",";
            if (componentStyle.getClickEvent() != null) tellrawString += "\"clickEvent\":{\"action\":\"" +
                    componentStyle.getClickEvent().getAction() + "\",\"value\":\"" +componentStyle.getClickEvent().getValue() + "\"},";
            if (componentStyle.getColor() != null) tellrawString += "\"color\":\"" + componentStyle.getColor().toString() + "\",";
            if (tellrawString.endsWith(",")) tellrawString = tellrawString.substring(0, tellrawString.length() - 1);
            tellrawString += "},";
        }
        if (tellrawString.endsWith(",")) tellrawString = tellrawString.substring(0, tellrawString.length() - 1);
        tellrawString += "]";
        return tellrawString;
    }

    private static MutableComponent addAllComponents(MutableComponent targetComponent, Component sourceComponent, Integer start){
        List<Component> newMessageComponents = new ArrayList<>();
        List<Component> oldMessageComponents = sourceComponent.toFlatList();
        for (Integer i = start; i < sourceComponent.toFlatList().toArray().length; i++) {
            newMessageComponents.add(oldMessageComponents.get(i));
        }
        for (Component newComponent : newMessageComponents) {
            targetComponent.append(newComponent);
        }
        return targetComponent;
    }

    private static Component handlePlayerMessage(ClientChatReceivedEvent e){
        //get the message minus the prefix
        String message = e.getMessage().getString().substring(4);
        Map<String, RankReplacement> rankToRankReplacement =  new HashMap<String, RankReplacement>(){
            {
                put("Rock", new RankReplacement("Rock",
                        Arrays.asList(
                                MutableComponent.create(new LiteralContents("[")).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("FFFFFF", 16)))),
                                MutableComponent.create(new LiteralContents("R")).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("B6A136", 16)))),
                                MutableComponent.create(new LiteralContents("]")).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("FFFFFF", 16))))
                        )));
                put("Trainer", new RankReplacement("Trainer",
                        Arrays.asList(
                                MutableComponent.create(new LiteralContents("[")).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("FFFFFF", 16)))),
                                MutableComponent.create(new LiteralContents("T")).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("1CFF1F", 16)))),
                                MutableComponent.create(new LiteralContents("]")).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("FFFFFF", 16))))
                        )));
                put("Water", new RankReplacement("Water",
                        Arrays.asList(
                                MutableComponent.create(new LiteralContents("[")).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("FFFFFF", 16)))),
                                MutableComponent.create(new LiteralContents("W")).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("415E9D", 16)))),
                                MutableComponent.create(new LiteralContents("]")).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("FFFFFF", 16))))
                        )));
                put("Admin", new RankReplacement("Admin",
                        Arrays.asList(
                                MutableComponent.create(new LiteralContents("[")).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("FFFFFF", 16)))),
                                MutableComponent.create(new LiteralContents("A")).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("E22C21", 16)))),
                                MutableComponent.create(new LiteralContents("]")).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("FFFFFF", 16))))
                        )));
                put("Beta", new RankReplacement("Beta",
                        Arrays.asList(
                                MutableComponent.create(new LiteralContents("[")).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("FFFFFF", 16)))),
                                MutableComponent.create(new LiteralContents("B")).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("DC8AD5", 16)))),
                                MutableComponent.create(new LiteralContents("]")).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("FFFFFF", 16))))
                        )));
                put("Architect", new RankReplacement("Architect",
                        Arrays.asList(
                                MutableComponent.create(new LiteralContents("[")).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("FFFFFF", 16)))),
                                MutableComponent.create(new LiteralContents("A")).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("FB01A6", 16)))),
                                MutableComponent.create(new LiteralContents("]")).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("FFFFFF", 16))))
                        )));
                put("Electric", new RankReplacement("Electric",
                        Arrays.asList(
                                MutableComponent.create(new LiteralContents("[")).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("FFFFFF", 16)))),
                                MutableComponent.create(new LiteralContents("E")).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("FFA300", 16)))),
                                MutableComponent.create(new LiteralContents("]")).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("FFFFFF", 16))))
                        )));
                put("Mod", new RankReplacement("Mod",
                        Arrays.asList(
                                MutableComponent.create(new LiteralContents("[")).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("FFFFFF", 16)))),
                                MutableComponent.create(new LiteralContents("M")).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("0E66FB", 16)))),
                                MutableComponent.create(new LiteralContents("]")).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("FFFFFF", 16))))
                        )));
                put("Fire", new RankReplacement("Fire",
                        Arrays.asList(
                                MutableComponent.create(new LiteralContents("[")).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("FFFFFF", 16)))),
                                MutableComponent.create(new LiteralContents("F")).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("9C0000", 16)))),
                                MutableComponent.create(new LiteralContents("]")).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("FFFFFF", 16))))
                        )));
                put("Head-Admin", new RankReplacement("Head-Admin",
                        Arrays.asList(
                                MutableComponent.create(new LiteralContents("[")).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("FFFFFF", 16)))),
                                MutableComponent.create(new LiteralContents("H")).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("585757", 16)))),
                                MutableComponent.create(new LiteralContents("A")).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("585757", 16)))),
                                MutableComponent.create(new LiteralContents("]")).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("FFFFFF", 16))))
                        )));
                put("Owner", new RankReplacement("Owner",
                        Arrays.asList(
                                MutableComponent.create(new LiteralContents("[")).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("FFFFFF", 16)))),
                                MutableComponent.create(new LiteralContents("O")).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("FB0800", 16)))),
                                MutableComponent.create(new LiteralContents("W")).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("FB0800", 16)))),
                                MutableComponent.create(new LiteralContents("N")).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("FB0800", 16)))),
                                MutableComponent.create(new LiteralContents("]")).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("FFFFFF", 16))))
                        )));
                put("Head-Staff", new RankReplacement("Head-Staff",
                        Arrays.asList(
                                MutableComponent.create(new LiteralContents("[")).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("FFFFFF", 16)))),
                                MutableComponent.create(new LiteralContents("H")).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("FB0EF9", 16)))),
                                MutableComponent.create(new LiteralContents("S")).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("FB0EF9", 16)))),
                                MutableComponent.create(new LiteralContents("]")).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("FFFFFF", 16))))
                        )));
                put("Head-Leader", new RankReplacement("Head-Leader",
                        Arrays.asList(
                                MutableComponent.create(new LiteralContents("[")).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("FFFFFF", 16)))),
                                MutableComponent.create(new LiteralContents("H")).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("927046", 16)))),
                                MutableComponent.create(new LiteralContents("L")).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("927046", 16)))),
                                MutableComponent.create(new LiteralContents("]")).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("FFFFFF", 16))))
                        )));
                put("Developer", new RankReplacement("Developer",
                        Arrays.asList(
                                MutableComponent.create(new LiteralContents("[")).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("FFFFFF", 16)))),
                                MutableComponent.create(new LiteralContents("D")).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("8D138C", 16)))),
                                MutableComponent.create(new LiteralContents("]")).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("FFFFFF", 16))))
                        )));
                put("Ice", new RankReplacement("Ice",
                        Arrays.asList(
                                MutableComponent.create(new LiteralContents("[")).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("FFFFFF", 16)))),
                                MutableComponent.create(new LiteralContents("I")).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("11D0DC", 16)))),
                                MutableComponent.create(new LiteralContents("]")).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt("FFFFFF", 16))))
                        )));
            }
        };

        MutableComponent newMessage = null;

        Pattern pattern = Pattern.compile("\\[(.*?)\\]");
        Matcher matcher = pattern.matcher(message);
        List<Component> oldMessageComponents = e.getMessage().toFlatList();
        List<Component> newMessageComponents = new ArrayList<>();
        //match on the rank tag
        if (matcher.find()) {
            RankReplacement replacement = rankToRankReplacement.get(matcher.group(1));
            newMessage = replacement.rankTag.get(0);
            for(int i = 1; i < replacement.getRankTag().size(); i++) {
                newMessage.append(replacement.getRankTag().get(i));
            }
            newMessage.append(MutableComponent.create(new LiteralContents(" ")));

            for (Integer i = replacement.rankName.length() + 3; i < e.getMessage().toFlatList().toArray().length; i++) {
                newMessageComponents.add(oldMessageComponents.get(i));
            }
            return addAllComponents(newMessage, e.getMessage(), replacement.rankName.length() + 3);
        }
        else{
            //if no tag was found, just strip the [G] and return the initial message
            return addAllComponents(MutableComponent.create(new LiteralContents("")), e.getMessage(), 0 );
        }
    }

    private static String escapeNonAscii(String str) {

        StringBuilder retStr = new StringBuilder();
        for(int i=0; i<str.length(); i++) {
            int cp = Character.codePointAt(str, i);
            int charCount = Character.charCount(cp);
            if (charCount > 1) {
                i += charCount - 1; // 2.
                if (i >= str.length()) {
                    throw new IllegalArgumentException("truncated unexpectedly");
                }
            }
            if (str.charAt(i) == '\n'){
                retStr.append("\\n");
            }
            else if (cp < 128) {
                retStr.appendCodePoint(cp);
            } else {
                retStr.append(String.format("\\u%x", cp));
            }
        }
        return retStr.toString();
    }

    private static class RankReplacement {
        private String rankName;
        private List<MutableComponent> rankTag;

        public RankReplacement(String rankName, List<MutableComponent> rankTag) {
            this.rankName = rankName;
            this.rankTag = rankTag;
        }

        public String getRankName(){
            return this.rankName;
        }

        public void setRankName(String rankName){
            this.rankName = rankName;
        }

        public List<MutableComponent> getRankTag(){
            return this.rankTag;
        }

        public void setRankTag(List<MutableComponent> rankTag){
            this.rankTag = rankTag;
        }

    }

    public class TitleHandler{
        @SubscribeEvent
        public static void handleOverlay(RenderGuiOverlayEvent e){
//            if(e.getOverlay().id().toString().equals("minecraft:title_text")){
//                System.out.println(e);
//                System.out.println(e.getOverlay().id());
//            }

        }
    }
}