package com.af.telegram;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.*;
import java.util.HashSet;
import java.util.Set;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;

public class AFTelegramBot extends TelegramLongPollingBot {
    private static final String BOT_TOKEN = System.getenv("BOT_TOKEN") != null ? 
        System.getenv("BOT_TOKEN") : "7657839511:AAHDY5AO4R48Gb9oupeZxmcM8BEZa3-rbRo";
    private static final String BOT_USERNAME = System.getenv("BOT_USERNAME") != null ? 
        System.getenv("BOT_USERNAME") : "@u124557";
    private static final Set<String> authorizedPlayers = new HashSet<>();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String PERMISSIONS_FILE = "permissions.json";

    public AFTelegramBot() {
        loadPermissions();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            if (messageText.startsWith("/am give ")) {
                String playerName = messageText.substring(9).trim();
                addPermission(playerName);
                sendMessage(chatId, "✅ Права выданы игроку: " + playerName);
            } else if (messageText.equals("/am list")) {
                StringBuilder list = new StringBuilder("📋 Игроки с правами:\n");
                if (authorizedPlayers.isEmpty()) {
                    list.append("Пусто");
                } else {
                    for (String player : authorizedPlayers) {
                        list.append("• ").append(player).append("\n");
                    }
                }
                sendMessage(chatId, list.toString());
            } else if (messageText.startsWith("/am remove ")) {
                String playerName = messageText.substring(11).trim();
                removePermission(playerName);
                sendMessage(chatId, "❌ Права отозваны у игрока: " + playerName);
            } else if (messageText.equals("/start") || messageText.equals("/help")) {
                sendMessage(chatId, "🤖 Команды бота AF Mod:\n" +
                        "/am give <ник> - выдать права игроку\n" +
                        "/am list - список игроков с правами\n" +
                        "/am remove <ник> - отозвать права\n" +
                        "/help - помощь");
            } else {
                sendMessage(chatId, "❓ Неизвестная команда. Используйте /help для справки");
            }
        }
    }

    private void sendMessage(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void addPermission(String playerName) {
        authorizedPlayers.add(playerName);
        savePermissions();
    }

    private void removePermission(String playerName) {
        authorizedPlayers.remove(playerName);
        savePermissions();
    }

    private void loadPermissions() {
        try {
            if (Files.exists(Paths.get(PERMISSIONS_FILE))) {
                String json = new String(Files.readAllBytes(Paths.get(PERMISSIONS_FILE)));
                Type setType = new TypeToken<Set<String>>(){}.getType();
                Set<String> loaded = GSON.fromJson(json, setType);
                if (loaded != null) {
                    authorizedPlayers.addAll(loaded);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void savePermissions() {
        try {
            String json = GSON.toJson(authorizedPlayers);
            Files.write(Paths.get(PERMISSIONS_FILE), json.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() {
        return BOT_USERNAME;
    }

    @Override
    public String getBotToken() {
        return BOT_TOKEN;
    }
}
