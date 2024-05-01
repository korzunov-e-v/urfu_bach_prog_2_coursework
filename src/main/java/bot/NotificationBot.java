package bot;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;

import static bot.Messaging.*;
import static bot.Queries.addProduct;
import static bot.Queries.getOrCreateUser;

public class NotificationBot extends TelegramLongPollingBot {

    private final Map<Long, State> stateMap;

    public NotificationBot(String botToken) {
        super(botToken);
        this.stateMap = new HashMap<>();
    }

    @Override
    public void onUpdateReceived(Update update) {
        long userId;
        String username;
        if (update.getMessage() != null) {
            userId = update.getMessage().getFrom().getId();
            username = update.getMessage().getFrom().getUserName();
        } else {
            userId = update.getCallbackQuery().getFrom().getId();
            username = update.getCallbackQuery().getFrom().getUserName();
        }

        getOrCreateUser(userId, username);

        State state = stateMap.getOrDefault(userId, new State(userId, Menu.MAIN));
        stateMap.put(userId, state);

        if (update.hasMessage() && update.getMessage().getText().startsWith("/")) {
            onCommand(update, state);
        } else if (update.hasMessage() && update.getMessage().hasText()) {
            onText(update, state);
        } else if (update.hasCallbackQuery()) {
            onCallback(update, state);
        }
    }

    // TODO
    private void onText(Update update, State state) {
        long userId = update.getMessage().getFrom().getId();
        if (state.currentMenu == Menu.ADD_PRODUCTS) {
            if (update.getMessage().hasText()) {
                String productUrl = update.getMessage().getText();
                ProductCreationStatus status = addProduct(userId, state.groupId, productUrl);
                sendMessageAddProductSuccess(state, status);
                if (status != ProductCreationStatus.SUCCESS) {
                    sendReport(productUrl, status, userId);
                }
            } else {
                sendMessageAddProductUnexpected(state);
            }
        } else {
            sendMessageCurrentState(state);
        }
    }

    private void onCommand(Update update, State state) {
        String command = update.getMessage().getText();
        switch (command) {
            case "/start" -> sendMessageStart(state);
            case "/help" -> sendMessageHelp(state);
            default -> {
                sendMessageNotKnownCommand(state);
                sendMessageMainMenu(state);
            }
        }
    }


    // TODO
    private void onCallback(Update update, State state) {
        long userId = update.getCallbackQuery().getFrom().getId();


        // TODO: for debug
//        InlineKeyboardMarkup kbm = bot.Keyboards.getKeyboard(state);
        SendMessage message = new SendMessage();
        message.setChatId(userId);
        message.setText("Ответ на callback#" + update.getCallbackQuery().getId() +
                ", data:" + update.getCallbackQuery().getData());
//        message.setReplyMarkup(kbm);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }


        String[] callbackData = update.getCallbackQuery().getData().split("\\+");
        String callbackCommand = callbackData[0];
        String callbackArg = null;
        if (callbackData.length == 2) {
            callbackArg = callbackData[1];
        }

        switch (callbackCommand) {
            case "main" -> sendMessageMainMenu(state);
            case "settings" -> sendMessageSettings(state);
            case "all_groups" -> sendMessageAllGroups(state);
            case "add_group" -> sendMessageAddGroup(state);
            case "delete_group" -> sendMessageDeleteGroups(state);
            case "retrieve_group" -> sendMessageRetrieveGroup(state);
            case "all_products" -> sendMessageAllProducts(state);
            case "add_product" -> sendMessageAddProducts(state);
            case "delete_product" -> sendMessageDeleteProducts(state);
            case "retrieve_product" -> sendMessageRetrieveProduct(state);
            case "reset_product" -> sendMessageResetProduct(state);
//            case "toggle_notifications" -> // TODO
        }
    }

    private void sendMessage(SendMessage message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendMessageStart(State state) {
        state.currentMenu = Menu.MAIN;
        SendMessage message = getMessageStart(state);
        sendMessage(message);
    }

    private void sendMessageHelp(State state) {
        state.currentMenu = Menu.HELP;
        SendMessage message = getMessageHelp(state);
        sendMessage(message);
    }

    private void sendMessageSettings(State state) {
        state.currentMenu = Menu.SETTINGS;
        SendMessage message = getMessageSettings(state);
        sendMessage(message);
    }

    private void sendMessageNotKnownCommand(State state) {
        state.currentMenu = Menu.MAIN;
        SendMessage message = getMessageNotKnownCommand(state);
        sendMessage(message);
    }

    private void sendMessageMainMenu(State state) {
        state.currentMenu = Menu.MAIN;
        SendMessage message = getMessageMainMenu(state);
        sendMessage(message);
    }

    private void sendMessageAllGroups(State state) {
        state.currentMenu = Menu.GROUPS;
        SendMessage message = getMessageAllGroups(state);
        sendMessage(message);
    }

    private void sendMessageAddGroup(State state) {
        state.currentMenu = Menu.GROUPS;
        SendMessage message = getMessageAddGroup(state);
        sendMessage(message);
    }

    private void sendMessageDeleteGroups(State state) {
        state.currentMenu = Menu.GROUPS;
        SendMessage message = getMessageDeleteGroups(state);
        sendMessage(message);
    }

    private void sendMessageRetrieveGroup(State state) {
        state.currentMenu = Menu.RETRIEVE_GROUP;
        SendMessage message = getMessageRetrieveGroup(state);
        sendMessage(message);
    }

    private void sendMessageAllProducts(State state) {
        state.currentMenu = Menu.MAIN;
        SendMessage message = getMessageAllProducts(state);
        sendMessage(message);
    }

    private void sendMessageAddProducts(State state) {
        state.currentMenu = Menu.RETRIEVE_GROUP;
        SendMessage message = getMessageAddProducts(state);
        sendMessage(message);
    }

    private void sendMessageDeleteProducts(State state) {
        state.currentMenu = Menu.RETRIEVE_GROUP;
        SendMessage message = getMessageDeleteProducts(state);
        sendMessage(message);
    }

    private void sendMessageRetrieveProduct(State state) {
        state.currentMenu = Menu.RETRIEVE_GROUP;
        SendMessage message = getMessageRetrieveProduct(state);
        sendMessage(message);
    }

    private void sendMessageResetProduct(State state) {
        state.currentMenu = Menu.RETRIEVE_GROUP;
        SendMessage message = getMessageResetProduct(state);
        sendMessage(message);
    }

    private void sendMessageAddProductSuccess(State state, ProductCreationStatus status) {
        SendMessage message = getMessageAddProductSuccess(state, status);
        sendMessage(message);
    }

    private void sendMessageAddProductUnexpected(State state) {
        state.currentMenu = Menu.RETRIEVE_GROUP;
        SendMessage message = getMessageAddProductUnexpected(state);
        sendMessage(message);
    }

    private void sendMessageCurrentState(State state) {
        switch (state.currentMenu) {
            case MAIN -> sendMessageMainMenu(state);
            case HELP -> sendMessageHelp(state);
            case GROUPS -> sendMessageAllGroups(state);
            case ADD_PRODUCTS -> sendMessageAddProducts(state);
            case RETRIEVE_GROUP -> sendMessageRetrieveGroup(state);
            case SETTINGS -> sendMessageSettings(state);
            case ALL_PRODUCTS -> sendMessageAllProducts(state);
            case RESET_PRODUCTS -> sendMessageResetProduct(state);
            case DELETE_PRODUCTS -> sendMessageDeleteProducts(state);
        }
    }

    // TODO
    private void sendReport(String productUrl, ProductCreationStatus status, long userId) {
        SendMessage message = new SendMessage();
        message.setChatId(Constants.ADMIN_ID);
        message.setText("Report:\n\nUserID:" + userId + "\nstatus:" + status + "productUrl:" + productUrl);
        sendMessage(message);
    }

    @Override
    public String getBotUsername() {
        return System.getenv("BOT_NAME");
    }

    static class State {

        long userId;
        Menu currentMenu;
        Integer groupId = null;
        int page;


        public State(long userId) {
            this.userId = userId;
            this.currentMenu = Menu.MAIN;
            this.page = 0;
        }

        public State(long userId, Menu currentMenu) {
            this.userId = userId;
            this.currentMenu = currentMenu;
            this.page = 0;
        }

        public State(long userId, Menu currentMenu, int page) {
            this.userId = userId;
            this.currentMenu = currentMenu;
            this.page = page;
        }
    }

    enum ProductCreationStatus {
        SUCCESS,
        FAILED,
        UNEXPECTED_MARKET,
        UNEXPECTED_URL,
        NO_PRODUCT,
    }

    enum Menu {
        MAIN("main"),
        GROUPS("groups"),
        RETRIEVE_GROUP("retrieve_group"),
        ALL_PRODUCTS("all_products"),
        ADD_PRODUCTS("add_products"),
        DELETE_PRODUCTS("delete_products"),
        RESET_PRODUCTS("reset_products"),
        SETTINGS("settings"),
        HELP("help");

        private final String command;

        public String getCommand() {
            return command;
        }

        Menu(String command) {
            this.command = command;
        }
    }
}