package bot;

import database.HibernateUtil;
import database.models.Group;
import database.models.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;

import static bot.Queries.getGroups;
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

        User user = getOrCreateUser(userId, username);

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

    private void onText(Update update, State state) {
        long userId = update.getMessage().getFrom().getId();
        if (state.currentMenu == Menu.ADD_PRODUCT) {
            if (update.getMessage().hasText()) {
                String productUrl = update.getMessage().getText();
                ProductAddStatus status = addProduct(userId, productUrl);
                sendMessageAddProductSuccess(userId, status);
                if (status != ProductAddStatus.SUCCESS) {
                    sendReport(productUrl, status, userId);
                }
            } else {
                sendMessageAddProductUnexpected(userId);
            }
        } else {
            sendMessageCurrentState(update, state);
        }
    }

    private void onCommand(Update update, State state) {
        String command = update.getMessage().getText();
        if (command.equals("/start")) {
            sendMessageStart(update, state);
        } else if (command.equals("/help")) {
            sendMessageHelp(update, state);
        } else {
            sendMessageNotKnownCommand(update, state);
            sendMessageMainMenu(update, state);
        }
    }

    private void onCallback(Update update, State state) {
        long userId = update.getCallbackQuery().getFrom().getId();


        // TODO: rework
//        InlineKeyboardMarkup kbm = bot.Keyboards.getKeyboard(state);
        SendMessage message = new SendMessage();
        message.setChatId(state.userId);
        message.setText("Ответ на callback#" + update.getCallbackQuery().getId() +
                ", data:" + update.getCallbackQuery().getData()); // todo: write prod message start
//        message.setReplyMarkup(kbm);

        String[] callbackData = update.getCallbackQuery().getData().split("\\+");
        String callbackCommand = callbackData[0];
        String callbackArg = null;
        if (callbackData.length == 2) {
            callbackArg = callbackData[1];
        }
        switch (callbackCommand) {
            case "main":
                sendMessageMainMenu(update, state);
                break;
            case "settings":
                break;
            case "all_groups":
                sendMessageAllGroups(userId);
                break;
            case "add_group":
                break;
            case "delete_group":
                break;
            case "retrieve_group":
                break;
            case "all_products":
                break;
            case "add_product":
                break;
            case "delete_product":
                break;
            case "reset_product":
                break;
            case "toggle_notifications":
                break;
        }


        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendMessageStart(Update update, State state) {
        InlineKeyboardMarkup kbm = Keyboards.getMainKeyboard();
        state.currentMenu = Menu.MAIN;

        SendMessage message = new SendMessage();
        message.setChatId(update.getMessage().getChatId());
        message.setText("Привет. Это стартовое сообщение"); // todo: write prod message start
        message.setReplyMarkup(kbm);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendMessageHelp(Update update, State state) {
        InlineKeyboardMarkup kbm = Keyboards.getHelpKeyboard();
        state.currentMenu = Menu.HELP;

        SendMessage message = new SendMessage();
        message.setChatId(update.getMessage().getChatId());
        message.setText("Привет. Это справка."); // todo: write prod message start
        message.setReplyMarkup(kbm);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendMessageNotKnownCommand(Update update, State state) {
        state.currentMenu = Menu.MAIN;
        SendMessage message = new SendMessage();
        message.setChatId(update.getMessage().getFrom().getId());
        message.setText("Бот такой команды не знает =(.");
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendMessageMainMenu(Update update, State state) {
        InlineKeyboardMarkup kbm = Keyboards.getMainKeyboard();
        state.currentMenu = Menu.MAIN;
        SendMessage message = new SendMessage();
        message.setChatId(update.getMessage().getFrom().getId());
        message.setText("Это главное меню"); // todo: write prod message start
        message.setReplyMarkup(kbm);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendMessageAllGroups(long userId) {
        List<Group> groups = getGroups(userId);
        System.out.println(groups);
    }

    private ProductAddStatus addProduct(long userId, String productUrl) {
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        Session session = sessionFactory.openSession();
        // TODO: check productUrl
        // TODO: add product to db
//        session.persist(new Product()); // TODO: создать конструктор
        return ProductAddStatus.SUCCESS;
    }

    private void sendMessageAddProductSuccess(long userId, ProductAddStatus status) {
//        InlineKeyboardMarkup kbm = bot.Keyboards.getCancelAddKeyboard();
        SendMessage message = new SendMessage();
        message.setChatId(userId);
        if (status == ProductAddStatus.SUCCESS) {
            message.setText("Продукт успешно добавлен."); // todo: write prod message start
        } else if (status == ProductAddStatus.UNEXPECTED_MARKET) {
            message.setText("Такой магазин не поддерживается. Администратор добавит возможность отслеживать цены в данном магазине в близжайшее время.");
        } else if (status == ProductAddStatus.UNEXPECTED_URL) {
            message.setText("Данная ссылка ведёт не на страницу товара.");
        } else if (status == ProductAddStatus.NO_PRODUCT) {
            message.setText("Данная страница не содержит товара. Возможно такого товара нет.");
        }
//        message.setReplyMarkup(kbm);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendMessageAddProductUnexpected(long userId) {
//        InlineKeyboardMarkup kbm = bot.Keyboards.getCancelAddKeyboard();
        SendMessage message = new SendMessage();
        message.setChatId(userId);
        message.setText("Такого сообщения не ожидалось"); // todo: write prod message start
//        message.setReplyMarkup(kbm);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendMessageCurrentState(Update update, State state) {
        // TODO: хз как это сделать
//        InlineKeyboardMarkup kbm = bot.Keyboards.getCancelAddKeyboard();
        SendMessage message = new SendMessage();
        message.setChatId(update.getMessage().getFrom().getId());
        message.setText("Такого сообщения не ожидалось"); // todo: write prod message start
//        message.setReplyMarkup(kbm);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendReport(String productUrl, ProductAddStatus status, long userId) {
        SendMessage message = new SendMessage();
        message.setChatId(Constants.ADMIN_ID);
        message.setText("Report:\n\nUserID:" + userId + "\nstatus:" + status + "productUrl:" + productUrl);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
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

    private enum ProductAddStatus {
        SUCCESS,
        UNEXPECTED_MARKET,
        UNEXPECTED_URL,
        NO_PRODUCT,
    }

    private enum Menu {
        MAIN,
        GROUPS,
        GROUP_RETRIEVE,
        ALL,
        ADD_PRODUCT,
        DELETE_PRODUCT,
        RESET_PRODUCT,
        SETTINGS,
        HELP
    }
}