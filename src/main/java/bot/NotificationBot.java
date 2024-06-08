package bot;

import database.HibernateUtil;
import database.models.Group;
import database.models.Product;
import database.models.User;
import mongo.MongoUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.resource.transaction.spi.TransactionStatus;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
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
        long userTgId;
        String username;
        if (update.getMessage() != null) {
            userTgId = update.getMessage().getFrom().getId();
            username = update.getMessage().getFrom().getUserName();
        } else {
            userTgId = update.getCallbackQuery().getFrom().getId();
            username = update.getCallbackQuery().getFrom().getUserName();
        }

        Session session = sessionFactory.getCurrentSession();
        session.getTransaction().begin();
        getOrCreateUser(userTgId, username);
        session.getTransaction().commit();

        State state = stateMap.getOrDefault(userTgId, new State(userTgId, Menu.MAIN));
        stateMap.put(userTgId, state);

        try {
            if (update.hasMessage() && update.getMessage().getText().startsWith("/")) {
                onCommand(update, state);
            } else if (update.hasMessage() && update.getMessage().hasText()) {
                onText(update, state);
            } else if (update.hasCallbackQuery()) {
                onCallback(update, state);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            session = sessionFactory.getCurrentSession();
            Transaction lastTransaction = session.getTransaction();
            if (lastTransaction.getStatus() == TransactionStatus.ACTIVE) {
                lastTransaction.rollback();
            }
            session.close();
        }
    }

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
        long userTgId = update.getCallbackQuery().getFrom().getId();

        // TODO: for debug
        SendMessage message = new SendMessage();
        message.setChatId(userTgId);
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
            case DELETE_GROUP -> {
                assert callbackArg != null;
                processDeleteGroup(state, Long.parseLong(callbackArg));
            }
            case RETRIEVE_GROUP -> {
                assert callbackArg != null;
                processRetrieveGroup(state, Long.parseLong(callbackArg));
            }
            case ALL_PRODUCTS -> processAllProducts(state);
            case ADD_PRODUCTS -> processAddProductsMenu(state);
            case DELETE_PRODUCTS -> processDeleteProductsMenu(state);
            case DELETE_PRODUCT -> {
                assert callbackArg != null;
                processDeleteProduct(state, Long.parseLong(callbackArg));
            }
            case RETRIEVE_PRODUCT -> processRetrieveProduct(state);
            case RESET_PRODUCTS -> processResetProductsMenu(state);
            case RESET_PRODUCT -> processResetProduct(state, Long.parseLong(callbackArg));
            case TOGGLE_NOTIFICATIONS -> processToggleNotifications(state);
        }
        AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery();
        answerCallbackQuery.setCallbackQueryId(update.getCallbackQuery().getId());
        answerCallbackQuery.setShowAlert(false);
        sendAnswerCallback(answerCallbackQuery);
    }

    public void sendNotification(long tgId, String productName, double lastPrice, double newPrice) {
        SendMessage message = getNotificationMessage(tgId, productName, lastPrice, newPrice);
        sendMessage(message);
    }

    private void sendMessage(SendMessage message, State state) {
        try {
            Message mes = execute(message);
            state.lastMessageId = mes.getMessageId();
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendMessage(SendMessage message) {
        try {
            Message mes = execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendAnswerCallback(AnswerCallbackQuery message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void processStartMenu(State state) {
        state.currentMenu = Menu.MAIN;
        SendMessage message = getMessageStart(state);
        sendMessage(message, state);
    }

    private void processHelpMenu(State state) {
        state.currentMenu = Menu.HELP;
        SendMessage message = getMessageHelp(state);
        sendMessage(message, state);
    }

    private void processSettingsMenu(State state) {
        state.currentMenu = Menu.SETTINGS;

        Session session = sessionFactory.getCurrentSession();
        Transaction transaction = session.beginTransaction();
        User user = getUser(state.userTgId);
        boolean notif = user.isEnableNotifications();
        transaction.commit();

        SendMessage message = getMessageSettings(state, notif);
        sendMessage(message, state);
    }

    private void processNotKnownCommand(State state) {
        state.currentMenu = Menu.MAIN;
        SendMessage message = getMessageNotKnownCommand(state);
        sendMessage(message, state);
    }

    private void processMainMenu(State state) {
        state.currentMenu = Menu.MAIN;
        SendMessage message = getMessageMainMenu(state);
        sendMessage(message, state);
    }

    private void processAllGroupsMenu(State state) {
        state.currentMenu = Menu.ALL_GROUPS;

        Session session = sessionFactory.getCurrentSession();
        Transaction transaction = session.beginTransaction();
        List<Group> groups = getGroups(state.userTgId);

        SendMessage message = getMessageAllGroups(state, groups);
        sendMessage(message, state);
        transaction.commit();
    }

    private void processAddGroupsMenu(State state) {
        state.currentMenu = Menu.ADD_GROUPS;

        Session session = sessionFactory.getCurrentSession();
        Transaction transaction = session.beginTransaction();
        List<Group> groups = getGroups(state.userTgId);
        transaction.commit();

        SendMessage message = getMessageAddGroups(state, groups);
        sendMessage(message, state);
    }

    private void processAddGroup(State state, String groupName) {
        state.currentMenu = Menu.ADD_GROUPS;

        Session session = sessionFactory.getCurrentSession();
        Transaction transaction = session.beginTransaction();
        GroupCreationStatus status = addGroup(state.userTgId, groupName);
        transaction.commit();

        SendMessage message;
        if (transaction.getStatus() == COMMITTED) {
            message = getMessageAddGroupSuccess(state, status);
        } else {
            message = getMessageError(state);
        }
        sendMessage(message, state);
    }

    private void processDeleteGroupsMenu(State state) {
        state.currentMenu = Menu.DELETE_GROUPS;

        Session session = sessionFactory.getCurrentSession();
        Transaction transaction = session.beginTransaction();
        List<Group> groups = getGroups(state.userTgId);
        transaction.commit();

        SendMessage message;
        if (transaction.getStatus() == COMMITTED) {
            message = getMessageDeleteGroups(state, groups);
        } else {
            message = getMessageError(state);
        }
        sendMessage(message, state);
    }

    private void processDeleteGroup(State state, long groupId) {
        state.currentMenu = Menu.DELETE_GROUPS;

        Session session = sessionFactory.getCurrentSession();
        Transaction transaction = session.beginTransaction();
        SendMessage message;

        Group group = session.get(Group.class, groupId);
        DeletionStatus status = deleteGroup(state.userTgId, groupId);
        transaction.commit();

        if (transaction.getStatus() == COMMITTED) {
            String groupName = group.getName();
            message = getMessageDeleteGroupSuccess(state, status, groupName);
        } else {
            message = getMessageError(state);
        }

        sendMessage(message, state);
        sendMessageCurrentState(state);
    }

    private void processRetrieveGroup(State state, long groupId) {
        state.currentMenu = Menu.RETRIEVE_GROUP;
        state.groupId = groupId;

        Session session = sessionFactory.getCurrentSession();
        Transaction transaction = session.beginTransaction();
        Group group = session.get(Group.class, groupId);
        List<Product> products = group.getProducts().stream().toList();
        transaction.commit();

        SendMessage message = getMessageRetrieveGroup(state, group, products);
        sendMessage(message, state);
    }

    private void processAllProducts(State state) {
        state.currentMenu = Menu.ALL_PRODUCTS;
        state.groupId = null;
        SendMessage message = getMessageAllProducts(state);
        sendMessage(message, state);
    }

    private void processAddProductsMenu(State state) {
        state.currentMenu = Menu.ADD_PRODUCTS;
        SendMessage message = getMessageAddProducts(state);
        sendMessage(message, state);
    }

    private void processAddProduct(State state, String productUrl) {
        Session session = sessionFactory.getCurrentSession();
        Transaction transaction = session.beginTransaction();
        ProductCreationStatus status = addProduct(state.userTgId, state.groupId, productUrl);
        transaction.commit();

        SendMessage message;
        if (transaction.getStatus() == COMMITTED) {
            message = getMessageAddProductSuccess(state, status);
        } else {
            message = getMessageError(state);
        }
        sendMessage(message, state);
    }

    private void processDeleteProduct(State state, long productId) {
        state.currentMenu = Menu.DELETE_PRODUCTS;

        Session session = sessionFactory.getCurrentSession();
        Transaction transaction1 = session.beginTransaction();
        DeletionStatus status = deleteProduct(state.userTgId, productId);
        transaction1.commit();

        session = sessionFactory.getCurrentSession();
        Transaction transaction2 = session.beginTransaction();
        Group group = session.get(Group.class, state.groupId);
        List<Product> products = group.getProducts().stream().toList();
        transaction2.commit();

        // todo: edit message
//        EditMessageText.builder().messageId(state.lastMessageId).text()

        SendMessage message;
        if (transaction1.getStatus() == COMMITTED) {
            message = getMessageDeleteProductSuccess(state, status);
        } else {
            message = getMessageError(state);
        }

        sendMessage(message, state);
        sendMessageCurrentState(state);
    }

    private void processDeleteProductsMenu(State state) {
        state.currentMenu = Menu.DELETE_PRODUCTS;

        Session session = sessionFactory.getCurrentSession();

        Transaction transaction = session.beginTransaction();
        Group group = session.get(Group.class, state.groupId);
        List<Product> products = group.getProducts().stream().toList();
        transaction.commit();

        SendMessage message = getMessageDeleteProducts(state, products);
        sendMessage(message, state);
    }

    private void processRetrieveProduct(State state) {
        state.currentMenu = Menu.RETRIEVE_GROUP;
        SendMessage message = getMessageRetrieveProduct(state);
        sendMessage(message, state);
    }

    private void processResetProductsMenu(State state) {
        state.currentMenu = Menu.RESET_PRODUCTS;

        Session session = sessionFactory.getCurrentSession();

        Transaction transaction = session.beginTransaction();
        Group group = session.get(Group.class, state.groupId);
        List<Product> products = group.getProducts().stream().toList();
        transaction.commit();

        SendMessage message = getMessageResetProducts(state, products);
        sendMessage(message, state);
    }

    private void processResetProduct(State state, long productId) {
        state.currentMenu = Menu.RESET_PRODUCTS;

        MongoUtil.resetProduct(productId);

        Session session = sessionFactory.getCurrentSession();
        Transaction transaction = session.beginTransaction();
        Product product = session.get(Product.class, productId);
        transaction.commit();

        SendMessage message = getMessageResetProductsSuccess(state, product.getName());
        sendMessage(message, state);
        sendMessageCurrentState(state);
    }

    private void processToggleNotifications(State state) {
        Session session = sessionFactory.getCurrentSession();
        Transaction transaction = session.beginTransaction();
        changeSettingNotification(state.userTgId);
        transaction.commit();

        sendMessageCurrentState(state);
    }


    private void sendMessageAddProductSuccess(State state, ProductCreationStatus status) {
        SendMessage message = getMessageAddProductSuccess(state, status);
        sendMessage(message, state);
    }

    private void sendMessageAddProductUnexpected(State state) {
        state.currentMenu = Menu.RETRIEVE_GROUP;
        SendMessage message = getMessageAddProductUnexpected(state);
        sendMessage(message, state);
    }

    private void sendMessageCurrentState(State state) {
        switch (state.currentMenu) {
            case MAIN -> processMainMenu(state);
            case HELP -> processHelpMenu(state);
            case ALL_GROUPS -> processAllGroupsMenu(state);
            case ADD_GROUPS -> processAddGroupsMenu(state);
            case DELETE_GROUPS -> processDeleteGroupsMenu(state);
            case ADD_PRODUCTS -> processAddProductsMenu(state);
            case RETRIEVE_GROUP -> processRetrieveGroup(state, state.groupId);
            case SETTINGS -> processSettingsMenu(state);
            case ALL_PRODUCTS -> processAllProducts(state);
            case RESET_PRODUCTS -> processResetProductsMenu(state);
            case DELETE_PRODUCTS -> processDeleteProductsMenu(state);
        }
    }

//    // TODO
//    private void sendReport(String productUrl, ProductCreationStatus status, long userTgId) {
//        SendMessage message = new SendMessage();
//        message.setChatId(Constants.ADMIN_ID);
//        message.setText(
//                "Report:\n\nUserID:" + userTgId + "\nstatus:" + status + "productUrl:" + productUrl);
//        sendMessage(message, state);
//    }

    @Override
    public String getBotUsername() {
        return System.getenv("BOT_NAME");
    }

    static class State {

        long userTgId;
        Menu currentMenu;
        Long groupId = null;
        int page;
        Integer lastMessageId = null;


        public State(long userTgId) {
            this.userTgId = userTgId;
            this.currentMenu = Menu.MAIN;
            this.page = 0;
        }

        public State(long userTgId, Menu currentMenu) {
            this.userTgId = userTgId;
            this.currentMenu = currentMenu;
            this.page = 0;
        }

        public State(long userTgId, Menu currentMenu, int page) {
            this.userTgId = userTgId;
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
        FORBIDDEN,
    }

    enum GroupCreationStatus {
        SUCCESS,
        ALREADY_EXISTS,
    }

    enum DeletionStatus {
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
        TOGGLE_NOTIFICATIONS,
        HELP
    }
}