package bot;

import database.HibernateUtil;
import database.models.Group;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static bot.Messaging.*;
import static bot.Queries.*;
import static org.hibernate.resource.transaction.spi.TransactionStatus.COMMITTED;

public class NotificationBot extends TelegramLongPollingBot {

    private final Map<Long, State> stateMap;
    private static final SessionFactory sessionFactory = HibernateUtil.getSessionFactory();

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

        Session session = sessionFactory.getCurrentSession();
        session.getTransaction().begin();
        getOrCreateUser(userId, username);
        session.getTransaction().commit();

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
        String messageText = update.getMessage().getText();
        switch (state.currentMenu) {
            case ADD_PRODUCTS -> processAddProduct(state, messageText);
            case ADD_GROUPS -> processAddGroup(state, messageText);
            default -> sendMessageCurrentState(state);
        }
    }

    private void onCommand(Update update, State state) {
        String command = update.getMessage().getText();
        switch (command) {
            case "/start" -> processStartMenu(state);
            case "/help" -> processHelpMenu(state);
            default -> {
                processNotKnownCommand(state);
                processMainMenu(state);
            }
        }
    }


    // TODO
    private void onCallback(Update update, State state) {
        long userId = update.getCallbackQuery().getFrom().getId();

        // TODO: for debug
        SendMessage message = new SendMessage();
        message.setChatId(userId);
        message.setText(
                "Ответ на callback#" + update.getCallbackQuery().getId() + ", data:"
                        + update.getCallbackQuery().getData());
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

        Menu command = Menu.valueOf(callbackCommand);

        switch (command) {
            case MAIN -> processMainMenu(state);
            case SETTINGS -> processSettingsMenu(state);
            case ALL_GROUPS -> processAllGroupsMenu(state);
            case ADD_GROUPS -> processAddGroupsMenu(state);
            case DELETE_GROUPS -> processDeleteGroupsMenu(state);
            case DELETE_GROUP -> processDeleteGroup(state, callbackArg);
            case RETRIEVE_GROUP -> processRetrieveGroup(state);
            case ALL_PRODUCTS -> processAllProducts(state);
            case ADD_PRODUCTS -> processAddProductsMenu(state);
            case DELETE_PRODUCTS -> processDeleteProductsMenu(state);
//            case DELETE_PRODUCT -> processDeleteProduct(state);
            case RETRIEVE_PRODUCT -> processRetrieveProduct(state);
            case RESET_PRODUCTS -> processResetProductsMenu(state);
//            case RESET_PRODUCT -> processResetProduct(state);
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

    private void processStartMenu(State state) {
        state.currentMenu = Menu.MAIN;
        SendMessage message = getMessageStart(state);
        sendMessage(message);
    }

    private void processHelpMenu(State state) {
        state.currentMenu = Menu.HELP;
        SendMessage message = getMessageHelp(state);
        sendMessage(message);
    }

    private void processSettingsMenu(State state) {
        state.currentMenu = Menu.SETTINGS;
        SendMessage message = getMessageSettings(state);
        sendMessage(message);
    }

    private void processNotKnownCommand(State state) {
        state.currentMenu = Menu.MAIN;
        SendMessage message = getMessageNotKnownCommand(state);
        sendMessage(message);
    }

    private void processMainMenu(State state) {
        state.currentMenu = Menu.MAIN;
        SendMessage message = getMessageMainMenu(state);
        sendMessage(message);
    }

    private void processAllGroupsMenu(State state) {
        state.currentMenu = Menu.ALL_GROUPS;

        Session session = sessionFactory.getCurrentSession();
        Transaction transaction = session.beginTransaction();
        List<Group> groups = getGroups(state.userId);
        transaction.commit();

        SendMessage message;
        if (transaction.getStatus() == COMMITTED) {
            message = getMessageAllGroups(state, groups);
        } else {
            message = getMessageError(state);
        }
        sendMessage(message);
    }

    private void processAddGroupsMenu(State state) {
        state.currentMenu = Menu.ADD_GROUPS;

        Session session = sessionFactory.getCurrentSession();
        Transaction transaction = session.beginTransaction();
        List<Group> groups = getGroups(state.userId);
        transaction.commit();

        SendMessage message = getMessageAddGroups(state, groups);
        sendMessage(message);
    }

    private void processAddGroup(State state, String groupName) {
        state.currentMenu = Menu.ADD_GROUPS;

        Session session = sessionFactory.getCurrentSession();
        Transaction transaction = session.beginTransaction();
        GroupCreationStatus status = addGroup(state.userId, groupName);
        transaction.commit();

        SendMessage message;
        if (transaction.getStatus() == COMMITTED) {
            message = getMessageAddGroupSuccess(state, status);
        } else {
            message = getMessageError(state);
        }
        sendMessage(message);
    }

    private void processDeleteGroupsMenu(State state) {
        state.currentMenu = Menu.DELETE_GROUPS;

        Session session = sessionFactory.getCurrentSession();
        Transaction transaction = session.beginTransaction();
        List<Group> groups = getGroups(state.userId);
        transaction.commit();

        SendMessage message;
        if (transaction.getStatus() == COMMITTED) {
            message = getMessageDeleteGroups(state, groups);
        } else {
            message = getMessageError(state);
        }
        sendMessage(message);
    }

    private void processDeleteGroup(State state, String callbackArg) {
        state.currentMenu = Menu.DELETE_GROUPS;

        long groupId = Long.parseLong(callbackArg);

        Session session = sessionFactory.getCurrentSession();
        Transaction transaction = session.beginTransaction();
        String groupName = session.get(Group.class, groupId).getName();
        GroupDeletionStatus status = deleteGroup(state.userId, groupId);
        transaction.commit();

        SendMessage message;
        if (transaction.getStatus() == COMMITTED) {
            message = getMessageDeleteGroupSuccess(state, status, groupName);
        } else {
            message = getMessageError(state);
        }
        sendMessage(message);
        sendMessageCurrentState(state);
    }

    private void processRetrieveGroup(State state) {
        state.currentMenu = Menu.RETRIEVE_GROUP;
        SendMessage message = getMessageRetrieveGroup(state);
        sendMessage(message);
    }

    private void processAllProducts(State state) {
        state.currentMenu = Menu.MAIN;
        SendMessage message = getMessageAllProducts(state);
        sendMessage(message);
    }

    private void processAddProductsMenu(State state) {
        state.currentMenu = Menu.RETRIEVE_GROUP;
        SendMessage message = getMessageAddProducts(state);
        sendMessage(message);
    }

    private void processAddProduct(State state, String productUrl) {
        state.currentMenu = Menu.RETRIEVE_GROUP;

        Session session = sessionFactory.getCurrentSession();
        Transaction transaction = session.beginTransaction();
        ProductCreationStatus status = addProduct(state.userId, state.groupId, productUrl);
        transaction.commit();

        SendMessage message;
        if (transaction.getStatus() == COMMITTED) {
            message = getMessageAddProductSuccess(state, status);
        } else {
            message = getMessageError(state);
        }
        sendMessage(message);
    }

    private void processDeleteProductsMenu(State state) {
        state.currentMenu = Menu.RETRIEVE_GROUP;
        SendMessage message = getMessageDeleteProducts(state);
        sendMessage(message);
    }

    private void processRetrieveProduct(State state) {
        state.currentMenu = Menu.RETRIEVE_GROUP;
        SendMessage message = getMessageRetrieveProduct(state);
        sendMessage(message);
    }

    private void processResetProductsMenu(State state) {
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
            case MAIN -> processMainMenu(state);
            case HELP -> processHelpMenu(state);
            case ALL_GROUPS -> processAllGroupsMenu(state);
            case ADD_GROUPS -> processAddGroupsMenu(state);
            case DELETE_GROUPS -> processDeleteGroupsMenu(state);
            case ADD_PRODUCTS -> processAddProductsMenu(state);
            case RETRIEVE_GROUP -> processRetrieveGroup(state);
            case SETTINGS -> processSettingsMenu(state);
            case ALL_PRODUCTS -> processAllProducts(state);
            case RESET_PRODUCTS -> processResetProductsMenu(state);
            case DELETE_PRODUCTS -> processDeleteProductsMenu(state);
        }
    }

    // TODO
    private void sendReport(String productUrl, ProductCreationStatus status, long userId) {
        SendMessage message = new SendMessage();
        message.setChatId(Constants.ADMIN_ID);
        message.setText(
                "Report:\n\nUserID:" + userId + "\nstatus:" + status + "productUrl:" + productUrl);
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

    enum GroupCreationStatus {
        SUCCESS,
        ALREADY_EXISTS,
    }

    enum GroupDeletionStatus {
        SUCCESS,
        NOT_FOUND,
        FORBIDDEN
    }

    enum Menu {
        MAIN,
        ALL_GROUPS,
        RETRIEVE_GROUP,
        ADD_GROUPS,
        DELETE_GROUPS,
        DELETE_GROUP,
        ALL_PRODUCTS,
        ADD_PRODUCTS,
        RETRIEVE_PRODUCT,
        DELETE_PRODUCTS,
        DELETE_PRODUCT,
        RESET_PRODUCTS,
        RESET_PRODUCT,
        SETTINGS,
        HELP
    }
}